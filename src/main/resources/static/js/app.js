const form = document.getElementById("reading-form");
const sensorInput = document.getElementById("sensor-id");
const metricInput = document.getElementById("metric");
const valueInput = document.getElementById("value");
const recordedAtInput = document.getElementById("recorded-at");
const statusEl = document.getElementById("status");
const statsEl = document.getElementById("stats");
const readingsEl = document.getElementById("readings");
const refreshButton = document.getElementById("refresh");

const metricLabel = {
  TEMPERATURE: "Temperatura",
  HUMIDITY: "Humedad",
};

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function setStatus(message, type) {
  statusEl.textContent = message;
  statusEl.className = `form-status ${type || ""}`.trim();
}

function formatNumber(value) {
  if (value === null || value === undefined) {
    return "—";
  }
  return Number(value).toLocaleString("es-MX", { maximumFractionDigits: 4 });
}

function formatWhen(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.replace("T", " ");
  }
  return date.toLocaleString("es-MX");
}

async function readError(response) {
  try {
    const body = await response.json();
    if (body.errors) {
      return Object.values(body.errors).join(" ");
    }
    return body.message || "No se pudo completar la operación";
  } catch {
    return "No se pudo completar la operación";
  }
}

function renderStats(stats) {
  const items = [
    ["Lecturas", stats.count],
    ["Mínimo", formatNumber(stats.min)],
    ["Máximo", formatNumber(stats.max)],
    ["Promedio", formatNumber(stats.average)],
  ];
  statsEl.innerHTML = items
    .map(
      ([label, value]) => `
      <article class="stat">
        <span>${label}</span>
        <strong>${escapeHtml(value)}</strong>
      </article>`
    )
    .join("");
}

function renderReadings(readings) {
  if (!readings.length) {
    readingsEl.innerHTML = '<p class="muted">Este sensor todavía no tiene lecturas.</p>';
    return;
  }

  readingsEl.innerHTML = readings
    .map(
      (reading) => `
      <article class="product-row reading-row">
        <div>
          <h3>${escapeHtml(formatNumber(reading.value))}</h3>
          <div class="meta">
            <span class="tag">${escapeHtml(metricLabel[reading.metric] || reading.metric)}</span>
            <span>ID ${reading.id}</span>
          </div>
        </div>
        <p>${escapeHtml(formatWhen(reading.recordedAt))}</p>
      </article>`
    )
    .join("");
}

async function loadSensor(sensorId) {
  const encoded = encodeURIComponent(sensorId);
  const [readingsResponse, statsResponse] = await Promise.all([
    fetch(`/api/readings/sensor/${encoded}`),
    fetch(`/api/readings/sensor/${encoded}/stats`),
  ]);

  if (!readingsResponse.ok) {
    throw new Error(await readError(readingsResponse));
  }
  if (!statsResponse.ok) {
    throw new Error(await readError(statsResponse));
  }

  renderReadings(await readingsResponse.json());
  renderStats(await statsResponse.json());
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const sensorId = sensorInput.value.trim();
  const payload = {
    sensorId,
    metric: metricInput.value,
    value: Number(valueInput.value),
  };
  if (recordedAtInput.value) {
    payload.recordedAt = recordedAtInput.value.length === 16
      ? `${recordedAtInput.value}:00`
      : recordedAtInput.value;
  }

  setStatus("Enviando lectura…");
  try {
    const response = await fetch("/api/readings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      throw new Error(await readError(response));
    }
    valueInput.value = "";
    recordedAtInput.value = "";
    setStatus("Lectura guardada.", "ok");
    await loadSensor(sensorId);
  } catch (error) {
    setStatus(error.message, "error");
  }
});

refreshButton.addEventListener("click", async () => {
  const sensorId = sensorInput.value.trim();
  if (!sensorId) {
    setStatus("Escribe el id del sensor.", "error");
    return;
  }
  setStatus("");
  try {
    await loadSensor(sensorId);
  } catch (error) {
    setStatus(error.message, "error");
  }
});

loadSensor(sensorInput.value.trim()).catch((error) => setStatus(error.message, "error"));

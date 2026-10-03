package com.epic.telemetry;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SensorReadingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void postWithoutTimestampReturnsCreatedReading() throws Exception {
		mockMvc.perform(post("/api/readings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "sensorId": "sensor-01",
								  "metric": "TEMPERATURE",
								  "value": 23.5
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.sensorId").value("sensor-01"))
				.andExpect(jsonPath("$.metric").value("TEMPERATURE"))
				.andExpect(jsonPath("$.value").value(23.5))
				.andExpect(jsonPath("$.recordedAt").isNotEmpty());
	}

	@Test
	void postKeepsClientTimestamp() throws Exception {
		mockMvc.perform(post("/api/readings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "sensorId": "sensor-01",
								  "metric": "HUMIDITY",
								  "value": 61.25,
								  "recordedAt": "2026-10-02T18:30:00"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.recordedAt").value("2026-10-02T18:30:00"))
				.andExpect(jsonPath("$.metric").value("HUMIDITY"));
	}

	@Test
	void postRejectsMissingFields() throws Exception {
		mockMvc.perform(post("/api/readings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "metric": "TEMPERATURE"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Error de validación"))
				.andExpect(jsonPath("$.errors.sensorId").exists())
				.andExpect(jsonPath("$.errors.value").exists());
	}

	@Test
	void postRejectsUnknownMetric() throws Exception {
		mockMvc.perform(post("/api/readings")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "sensorId": "sensor-01",
								  "metric": "PRESSURE",
								  "value": 1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El cuerpo de la petición no es válido"));
	}

	@Test
	void getReturnsEmptyListWhenSensorHasNoReadings() throws Exception {
		mockMvc.perform(get("/api/readings/sensor/sensor-99"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void getRejectsInvalidSensorId() throws Exception {
		mockMvc.perform(get("/api/readings/sensor/@@@"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Error de validación"))
				.andExpect(jsonPath("$.errors.sensorId").value("El id del sensor no es válido"));
	}

	@Test
	void getFiltersBySensorAndStatsMatch() throws Exception {
		postReading("sensor-01", "TEMPERATURE", "10.00", "2026-10-02T10:00:00");
		postReading("sensor-01", "TEMPERATURE", "30.00", "2026-10-02T11:00:00");
		postReading("sensor-02", "HUMIDITY", "40.00", "2026-10-02T12:00:00");

		mockMvc.perform(get("/api/readings/sensor/sensor-01"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].value").value(10.00))
				.andExpect(jsonPath("$[1].value").value(30.00));

		mockMvc.perform(get("/api/readings/sensor/sensor-01/stats"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sensorId").value("sensor-01"))
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.min").value(10.00))
				.andExpect(jsonPath("$.max").value(30.00))
				.andExpect(jsonPath("$.average").value(20.0000));
	}

	@Test
	void statsWithoutReadingsAreZero() throws Exception {
		mockMvc.perform(get("/api/readings/sensor/sensor-99/stats"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0))
				.andExpect(jsonPath("$.min").value(nullValue()))
				.andExpect(jsonPath("$.max").value(nullValue()))
				.andExpect(jsonPath("$.average").value(nullValue()));
	}

	private void postReading(String sensorId, String metric, String value, String recordedAt) throws Exception {
		String body = """
				{
				  "sensorId": "%s",
				  "metric": "%s",
				  "value": %s,
				  "recordedAt": "%s"
				}
				""".formatted(sensorId, metric, value, recordedAt);

		mockMvc.perform(post("/api/readings")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());
	}
}

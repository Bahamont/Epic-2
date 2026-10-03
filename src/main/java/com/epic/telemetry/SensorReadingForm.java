package com.epic.telemetry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SensorReadingForm {

	@NotBlank(message = "El id del sensor es obligatorio")
	@Pattern(regexp = SensorIds.PATTERN, message = SensorIds.MESSAGE)
	private String sensorId;

	@NotNull(message = "La métrica es obligatoria")
	private MetricType metric;

	@NotNull(message = "El valor medido es obligatorio")
	private BigDecimal value;

	private LocalDateTime recordedAt;

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public MetricType getMetric() {
		return metric;
	}

	public void setMetric(MetricType metric) {
		this.metric = metric;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(LocalDateTime recordedAt) {
		this.recordedAt = recordedAt;
	}
}

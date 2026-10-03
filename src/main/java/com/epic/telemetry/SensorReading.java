package com.epic.telemetry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensor_readings", indexes = {
		@Index(name = "idx_sensor_readings_sensor_id", columnList = "sensor_id")
})
public class SensorReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sensor_id", nullable = false, length = 64)
	private String sensorId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MetricType metric;

	@Column(name = "measured_value", nullable = false, precision = 12, scale = 4)
	private BigDecimal value;

	@Column(nullable = false)
	private LocalDateTime recordedAt;

	public SensorReading() {
	}

	@PrePersist
	void onCreate() {
		if (recordedAt == null) {
			recordedAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

package com.epic.telemetry;

import java.math.BigDecimal;

public class SensorReadingStats {

	private String sensorId;
	private long count;
	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal average;

	public SensorReadingStats() {
	}

	public SensorReadingStats(String sensorId, long count, BigDecimal min, BigDecimal max, BigDecimal average) {
		this.sensorId = sensorId;
		this.count = count;
		this.min = min;
		this.max = max;
		this.average = average;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public BigDecimal getAverage() {
		return average;
	}

	public void setAverage(BigDecimal average) {
		this.average = average;
	}
}

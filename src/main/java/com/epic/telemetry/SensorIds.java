package com.epic.telemetry;

public final class SensorIds {

	public static final String PATTERN = "^[A-Za-z0-9][A-Za-z0-9_\\-]{0,63}$";

	public static final String MESSAGE = "El id del sensor no es válido";

	private SensorIds() {
	}

	public static boolean isValid(String sensorId) {
		return sensorId != null && sensorId.matches(PATTERN);
	}
}

package com.epic.telemetry;

public class InvalidSensorIdException extends RuntimeException {

	public InvalidSensorIdException() {
		super(SensorIds.MESSAGE);
	}
}

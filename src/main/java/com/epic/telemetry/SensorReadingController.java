package com.epic.telemetry;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

	private final SensorReadingService sensorReadingService;

	public SensorReadingController(SensorReadingService sensorReadingService) {
		this.sensorReadingService = sensorReadingService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SensorReading register(@Valid @RequestBody SensorReadingForm form) {
		return sensorReadingService.register(form);
	}

	@GetMapping("/sensor/{sensorId}/stats")
	public SensorReadingStats statsBySensor(@PathVariable String sensorId) {
		return sensorReadingService.statsBySensorId(sensorId);
	}

	@GetMapping("/sensor/{sensorId}")
	public List<SensorReading> listBySensor(@PathVariable String sensorId) {
		return sensorReadingService.findBySensorId(sensorId);
	}
}

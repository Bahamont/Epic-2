package com.epic.telemetry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SensorReadingService {

	private final SensorReadingRepository sensorReadingRepository;

	public SensorReadingService(SensorReadingRepository sensorReadingRepository) {
		this.sensorReadingRepository = sensorReadingRepository;
	}

	public SensorReading register(SensorReadingForm form) {
		SensorReading reading = new SensorReading();
		reading.setSensorId(form.getSensorId());
		reading.setMetric(form.getMetric());
		reading.setValue(form.getValue());
		reading.setRecordedAt(form.getRecordedAt() != null ? form.getRecordedAt() : LocalDateTime.now());
		return sensorReadingRepository.save(reading);
	}

	public List<SensorReading> findBySensorId(String sensorId) {
		requireValidSensorId(sensorId);
		return sensorReadingRepository.findBySensorIdOrderByRecordedAtAsc(sensorId);
	}

	public SensorReadingStats statsBySensorId(String sensorId) {
		requireValidSensorId(sensorId);

		List<Object[]> rows = sensorReadingRepository.aggregateBySensorId(sensorId);
		if (rows.isEmpty() || rows.get(0) == null) {
			return new SensorReadingStats(sensorId, 0, null, null, null);
		}

		Object[] row = rows.get(0);
		long count = row[0] == null ? 0 : ((Number) row[0]).longValue();
		BigDecimal min = toBigDecimal(row[1]);
		BigDecimal max = toBigDecimal(row[2]);
		BigDecimal average = toBigDecimal(row[3]);
		if (average != null) {
			average = average.setScale(4, RoundingMode.HALF_UP);
		}
		return new SensorReadingStats(sensorId, count, min, max, average);
	}

	private void requireValidSensorId(String sensorId) {
		if (!SensorIds.isValid(sensorId)) {
			throw new InvalidSensorIdException();
		}
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal decimal) {
			return decimal;
		}
		if (value instanceof Number number) {
			return new BigDecimal(number.toString());
		}
		throw new IllegalStateException("Tipo numérico no soportado: " + value.getClass().getName());
	}
}

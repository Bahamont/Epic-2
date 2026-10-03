package com.epic.telemetry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

	List<SensorReading> findBySensorIdOrderByRecordedAtAsc(String sensorId);

	@Query("""
			select count(r), min(r.value), max(r.value), avg(r.value)
			from SensorReading r
			where r.sensorId = :sensorId
			""")
	List<Object[]> aggregateBySensorId(@Param("sensorId") String sensorId);
}

package fr.aquabian.slave.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class MockSensor implements ISensor {

    private final String sensorId;

    public MockSensor(){
        this.sensorId = UUID.randomUUID().toString();
    }

    @Override
    public Optional<Measure> getMeasure() {
        return Optional.of(new Measure(sensorId, Math.random()*30, Instant.now()));
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }
}

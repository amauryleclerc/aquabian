package fr.aquabian.master.projection.persistence.entity;

import java.io.Serializable;
import java.time.Instant;

public class MeasurePrimaryKey implements Serializable {
    public MeasurePrimaryKey() {

    }

    public MeasurePrimaryKey(SensorEntity sensor, Instant date) {
        this.sensor = sensor;
        this.date = date;
    }

    private SensorEntity sensor;
    private Instant date;


    public SensorEntity getSensor() {
        return sensor;
    }

    public Instant getDate() {
        return date;
    }
}

package fr.aquabian.master.projection.persistence.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurePrimaryKey that = (MeasurePrimaryKey) o;
        return Objects.equals(sensor, that.sensor) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sensor, date);
    }
}

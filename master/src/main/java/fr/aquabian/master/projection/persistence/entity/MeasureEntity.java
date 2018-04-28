package fr.aquabian.master.projection.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Data
@IdClass(MeasurePrimaryKey.class)
public class MeasureEntity implements Serializable {

    @Id
    private Instant date;

    @Id
    @ManyToOne
    private SensorEntity sensor;

    @Basic
    private Double value;

    @Override
    public String toString() {
        return "MeasureEntity{" +
                "date=" + date +
                ", value=" + value +
                '}';
    }
}

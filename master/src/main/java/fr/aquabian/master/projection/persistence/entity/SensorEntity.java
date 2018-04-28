package fr.aquabian.master.projection.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@Entity
@Data
public class SensorEntity implements Serializable {

    @Id
    private String id;

    @Basic
    private String name;

    @ManyToOne
    private DeviceEntity device;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "sensor")
    private Collection<MeasureEntity> measures = Collections.emptySet();


    @Override
    public String toString() {
        return "SensorEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

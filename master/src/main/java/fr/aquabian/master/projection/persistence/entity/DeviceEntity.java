package fr.aquabian.master.projection.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@Entity
@Data
public class DeviceEntity implements Serializable {

    @Id
    private String id;

    @Basic
    private String name;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "device")
    private Collection<SensorEntity> sensors = Collections.emptySet();


    @Override
    public String toString() {
        return "DeviceEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

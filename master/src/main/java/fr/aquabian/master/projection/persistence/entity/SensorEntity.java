package fr.aquabian.master.projection.persistence.entity;

import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
@Data
public class SensorEntity implements Serializable {

    @Id
    private String id;

    @Basic
    private String name;

    @ManyToOne
    private DeviceEntity device;

    @Override
    public String toString() {
        return "SensorEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

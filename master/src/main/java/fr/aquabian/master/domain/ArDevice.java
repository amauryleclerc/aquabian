package fr.aquabian.master.domain;

import fr.aquabian.api.domain.command.AquabianCommands;
import fr.aquabian.api.domain.event.AquabianEvents;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Collection;


@Aggregate
@Entity
public class ArDevice {
    private final static Logger LOGGER = LoggerFactory.getLogger(ArDevice.class);

    @Id
    @AggregateIdentifier
    private String id;

    @Basic
    private String name;


    @ElementCollection
    public Collection<String> sensors;

    public ArDevice() {
    }

    @CommandHandler
    public ArDevice(AquabianCommands.CreateDeviceCommand command) {
        AggregateLifecycle.apply(AquabianEvents.DeviceCreatedEvent.newBuilder()//
                .setId(command.getId())//
                .setName(command.getName())//
                .addAllSensors(command.getSensorsList())//
                .build());
    }


    @CommandHandler
    public void addSensor(AquabianCommands.AddSensorToDeviceCommand command) {
        AggregateLifecycle.apply(AquabianEvents.SensorAddedToDeviceEvent.newBuilder()//
                .setId(command.getId())//
                .setIdSensor(command.getIdSensor())//
                .build());
    }

    @EventSourcingHandler
    public void on(AquabianEvents.DeviceCreatedEvent event) {
        LOGGER.debug("Create device {} with name {}", event.getId(), event.getName());
        this.id = event.getId();
        this.name = event.getName();
        this.sensors = new ArrayList<>(event.getSensorsList());
    }

    @EventSourcingHandler
    public void on(AquabianEvents.SensorAddedToDeviceEvent event) {
        LOGGER.debug("Add sensor to device {} - {}", this.id, this.name);
        this.sensors.add(event.getIdSensor());
    }
}
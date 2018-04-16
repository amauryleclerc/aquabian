package fr.aquabian.master.domain;

import com.aquabian.api.domain.command.AquabianCommands;
import com.aquabian.api.domain.event.AquabianEvents;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Aggregate
@NoArgsConstructor
@Entity
public class ArSensor {
    private final static Logger LOGGER = LoggerFactory.getLogger(ArDevice.class);

    @Id
    @AggregateIdentifier
    private String id;

    @Basic
    private String deviceId;

    @Basic
    private String name;

    @Basic
    private Double lastValue;


    @CommandHandler
    public ArSensor(AquabianCommands.CreateSensorCommand command) {
        AggregateLifecycle.apply(AquabianEvents.SensorCreatedEvent.newBuilder()//
                .setId(command.getId())//
                .setName(command.getName())//
                .setDevice(command.getDevice())//
                .build());
    }


    @CommandHandler
    public void addSensor(AquabianCommands.AddMeasureCommand command) {
        AggregateLifecycle.apply(AquabianEvents.MeasureAddedEvent.newBuilder()//
                .setId(command.getId())//
                .setDate(command.getDate())//
                .setValue(command.getValue())//
                .build());
    }

    @EventSourcingHandler
    public void on(AquabianEvents.SensorCreatedEvent event) {
        LOGGER.info("Create sensor {} with name {}", event.getId(), event.getName());
        this.id = event.getId();
        this.name = event.getName();
        this.deviceId = event.getDevice();
    }

    @EventSourcingHandler
    public void on(AquabianEvents.MeasureAddedEvent event) {
        LOGGER.info("Add mesure {} from sensor {} - {}", event.getValue(), this.id, this.name);
        this.lastValue = event.getValue();
    }
}
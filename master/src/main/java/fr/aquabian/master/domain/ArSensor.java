package fr.aquabian.master.domain;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
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
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Aggregate
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

    public ArSensor(){

    }


    @CommandHandler
    public ArSensor(AquabianCommands.CreateSensorCommand command) {
        AggregateLifecycle.apply(AquabianEvents.SensorCreatedEvent.newBuilder()//
                .setId(command.getId())//
                .setName(command.getName())//
                .setDevice(command.getDevice())//
                .build());
    }


     @CommandHandler
    public void addMeasure(AquabianCommands.AddMeasureCommand command) {
        Timestamp timestamp = Timestamps.fromMillis(Instant.now().toEpochMilli());
        AggregateLifecycle.apply(AquabianEvents.MeasureAddedEvent.newBuilder()//
                .setId(command.getId())//
                .setDate(timestamp)//
                .setValue(command.getValue())//
                .build());
    }

    @CommandHandler
    public void rename(AquabianCommands.RenameSensorCommand command) {
        AggregateLifecycle.apply(AquabianEvents.SensorRenamedEvent.newBuilder()//
                .setId(command.getId())//
                .setName(command.getName())//
                .build());
    }


    @EventSourcingHandler
    public void on(AquabianEvents.SensorCreatedEvent event) {
        LOGGER.debug("Create sensor {} with name {}", event.getId(), event.getName());
        this.id = event.getId();
        this.name = event.getName();
        this.deviceId = event.getDevice();
    }

    @EventSourcingHandler
    public void on(AquabianEvents.MeasureAddedEvent event) {
        LOGGER.debug("Add mesure {} from sensor {} - {}", event.getValue(), this.id, this.name);
        this.lastValue = event.getValue();
    }


    @EventSourcingHandler
    public void on(AquabianEvents.SensorRenamedEvent event) {
        LOGGER.debug("Rename sensor {} - {}", this.id, event.getName());
        this.name = event.getName();
    }

}
package fr.aquabian.master.saga;

import fr.aquabian.api.domain.command.AquabianCommands;
import fr.aquabian.api.domain.event.AquabianEvents;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.callbacks.LoggingCallback;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.SagaLifecycle;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@Saga
public class DeviceSaga {

    @Autowired
    private transient CommandBus commandBus;



    @StartSaga
    @SagaEventHandler(associationProperty = "Id")
    public void handle(AquabianEvents.SensorCreatedEvent event) {
        SagaLifecycle.associateWith("idSensor", event.getId());
        // send the commands
        AquabianCommands.AddSensorToDeviceCommand command = AquabianCommands.AddSensorToDeviceCommand.newBuilder()//
                .setId(event.getDevice())//
                .setIdSensor(event.getId())//
                .build();
       this.commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);
    }

    @SagaEventHandler(associationProperty = "idSensor")
    public void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
        SagaLifecycle.end();
    }
}

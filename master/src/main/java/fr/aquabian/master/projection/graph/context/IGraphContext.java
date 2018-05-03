package fr.aquabian.master.projection.graph.context;

import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import io.reactivex.Observable;

public interface IGraphContext {

    Observable<SensorProjectionEvents.SensorProjectionEvent> getStream();

    default void handle(AquabianEvents.DeviceCreatedEvent event) {
    }


    default void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
    }


    default void handle(AquabianEvents.SensorCreatedEvent event) {
    }


    default void handle(AquabianEvents.MeasureAddedEvent event) {
    }

    default void handle(AquabianEvents.SensorRenamedEvent event) {
    }

}

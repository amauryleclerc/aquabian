package fr.aquabian.api;

import fr.aquabian.api.projection.command.SensorProjectionEvents;
import io.reactivex.Observable;

public interface ISensorProjectionEventStream {

    /**
     * Flux infini d'event de la projection Sensor
     * @return
     */
    Observable<SensorProjectionEvents.SensorProjectionEvent> getStream();
}

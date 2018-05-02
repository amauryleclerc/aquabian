package fr.aquabian.master.projection.graph;

import fr.aquabian.api.projection.command.SensorProjectionEvents;
import io.reactivex.Observable;

public interface IGraphProjection {

    Observable<SensorProjectionEvents.SensorProjectionEvent> getStream(final SensorProjectionEvents.GraphQuery graphQuery);
}

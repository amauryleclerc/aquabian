package fr.aquabian.master.projection.graph.context;

import fr.aquabian.api.projection.command.SensorProjectionEvents;
import fr.aquabian.master.projection.graph.GraphUtils;
import fr.aquabian.master.projection.persistence.repository.MeasureRepository;
import io.reactivex.Observable;

import java.time.Instant;

public class PastWindowContext implements IGraphContext {

    private final Instant dateMin;
    private final Instant dateMax;
    private final MeasureRepository measureRepository;

    public PastWindowContext(Instant dateMin, Instant dateMax, MeasureRepository measureRepository) {
        this.dateMin= dateMin;
        this.dateMax = dateMax;
        this.measureRepository = measureRepository;

    }
    @Override
    public Observable<SensorProjectionEvents.SensorProjectionEvent> getStream() {
        return GraphUtils.getCurrentState(() -> measureRepository.findByDateBetween(dateMin,dateMax)).toObservable().concatWith(Observable.never());
    }
}

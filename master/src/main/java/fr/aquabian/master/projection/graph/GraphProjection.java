package fr.aquabian.master.projection.graph;

import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import fr.aquabian.master.projection.graph.context.IGraphContext;
import fr.aquabian.master.projection.graph.context.PastWindowContext;
import fr.aquabian.master.projection.graph.context.SlidingWindowContext;
import fr.aquabian.master.projection.persistence.repository.MeasureRepository;
import fr.aquabian.toolkit.ProtoUtils;
import io.reactivex.Observable;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@ProcessingGroup("GraphProjection")
public class GraphProjection implements IGraphProjection {


    private final MeasureRepository measureRepository;

    private final List<IGraphContext> contexts = new CopyOnWriteArrayList<IGraphContext>();

    @Autowired
    public GraphProjection(MeasureRepository measureRepository) {
        this.measureRepository = measureRepository;
    }

    @EventHandler
    public void handle(AquabianEvents.DeviceCreatedEvent event) {
        contexts.forEach(c -> c.handle(event));
    }

    @EventHandler
    public void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
        contexts.forEach(c -> c.handle(event));
    }

    @EventHandler
    public void handle(AquabianEvents.SensorCreatedEvent event) {
        contexts.forEach(c -> c.handle(event));
    }

    @EventHandler
    public void handle(AquabianEvents.MeasureAddedEvent event) {
        contexts.forEach(c -> c.handle(event));
    }


    @Override
    public Observable<SensorProjectionEvents.SensorProjectionEvent> getStream(final SensorProjectionEvents.GraphQuery graphQuery) {

        if (graphQuery.hasPastWindowQuery()) {
            Instant dateMin = ProtoUtils.convertTimestampProtoToInstant(graphQuery.getPastWindowQuery().getDateMin());
            Instant dateMax = ProtoUtils.convertTimestampProtoToInstant(graphQuery.getPastWindowQuery().getDateMax());
            PastWindowContext pastWindowContext = new PastWindowContext(dateMin,dateMax,measureRepository);
            return pastWindowContext.getStream()//
                    .doOnSubscribe(s -> this.contexts.add(pastWindowContext))//
                    .doOnDispose(() -> this.contexts.remove(pastWindowContext));
        } else if (graphQuery.hasSlidingWindowQuery()) {
            SlidingWindowContext context = new SlidingWindowContext(graphQuery.getSlidingWindowQuery().getAfterglowSec(), measureRepository);
            return context.getStream()//
                    .doOnSubscribe(s -> this.contexts.add(context))//
                    .doOnDispose(() -> this.contexts.remove(context));
        } else {
            return Observable.error(new RuntimeException("No query found"));
        }

    }


}

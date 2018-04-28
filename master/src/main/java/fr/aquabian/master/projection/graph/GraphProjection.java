package fr.aquabian.master.projection.graph;

import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.master.projection.measure.MeasureProjection;
import fr.aquabian.master.projection.persistence.entity.MeasureEntity;
import fr.aquabian.master.projection.persistence.repository.MeasureRepository;
import io.reactivex.Observable;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Component
@ProcessingGroup("GraphProjection")
public class GraphProjection {


    private final MeasureRepository measureRepository;

    private final List<GraphContext> contexts = Collections.emptyList();

    @Autowired
    public GraphProjection(MeasureRepository measureRepository) {
        this.measureRepository = measureRepository;
    }

    @PostConstruct
    private void init() {
        List<MeasureEntity> list = measureRepository.findByDateAfter(Instant.now().minusSeconds(500));
        System.out.println("list = " + list);
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


    public Observable<MeasureProjection> getStream(int seconds) {
        GraphContext context = new GraphContext(seconds, measureRepository);
        return context.getStream()//
                .doOnSubscribe(s -> this.contexts.add(context))//
                .doOnComplete(() -> this.contexts.remove(context));
    }

    private class GraphContext {


        private final List<MeasureEntity> mesures;

        private GraphContext(int second, MeasureRepository measureRepository) {
            this.mesures = measureRepository.findByDateAfter(Instant.now().minusSeconds(second));
        }

        private Observable<MeasureProjection> getStream() {
            return null;
        }
        public void handle(AquabianEvents.DeviceCreatedEvent event) {
        }

        public void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
        }

        public void handle(AquabianEvents.SensorCreatedEvent event) {
        }

        public void handle(AquabianEvents.MeasureAddedEvent event) {
        }

    }
}

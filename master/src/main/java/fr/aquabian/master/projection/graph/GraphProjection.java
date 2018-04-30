package fr.aquabian.master.projection.graph;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.protobuf.util.Timestamps;
import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import fr.aquabian.master.projection.persistence.entity.MeasureEntity;
import fr.aquabian.master.projection.persistence.entity.MeasurePrimaryKey;
import fr.aquabian.master.projection.persistence.entity.SensorEntity;
import fr.aquabian.master.projection.persistence.repository.MeasureRepository;
import fr.aquabian.toolkit.ProtoUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Component
@ProcessingGroup("GraphProjection")
public class GraphProjection implements IGraphProjection {


    private final MeasureRepository measureRepository;

    private final List<GraphContext> contexts = new CopyOnWriteArrayList<GraphContext>();

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
    public Observable<SensorProjectionEvents.SensorProjectionEvent> getStream(long seconds) {
        GraphContext context = new GraphContext(seconds, measureRepository);
        return context.getStream()//
                .doOnSubscribe(s -> this.contexts.add(context))//
                .doOnDispose(() -> this.contexts.remove(context));
    }

    private class GraphContext {

        private final MeasureRepository measureRepository;
        private final long second;
        private Subject<SensorProjectionEvents.SensorProjectionEvent> eventSubject = PublishSubject.<SensorProjectionEvents.SensorProjectionEvent>create().toSerialized();
        private LoadingCache<MeasurePrimaryKey, SensorProjectionEvents.Measure> measures;

        private GraphContext(long second, MeasureRepository measureRepository) {
            this.measureRepository = measureRepository;
            this.second = second;
            this.measures = Caffeine.newBuilder()
                    .expireAfter(new Expiry<MeasurePrimaryKey, SensorProjectionEvents.Measure>() {
                        @Override
                        public long expireAfterCreate(MeasurePrimaryKey key, SensorProjectionEvents.Measure value, long currentTime) {
                            long milli = key.getDate().plusSeconds(second)
                                    .minus(System.currentTimeMillis(), ChronoUnit.MILLIS)
                                    .toEpochMilli();
                            return TimeUnit.MILLISECONDS.toNanos(milli);
                        }

                        @Override
                        public long expireAfterUpdate(MeasurePrimaryKey key, SensorProjectionEvents.Measure value, long currentTime, long currentDuration) {
                            return currentDuration;
                        }

                        @Override
                        public long expireAfterRead(MeasurePrimaryKey key, SensorProjectionEvents.Measure value, long currentTime, long currentDuration) {
                            return currentDuration;
                        }
                    }).removalListener((key, value, cause) -> {
                        eventSubject.onNext(SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                                .setRemoveMeasureEvent(SensorProjectionEvents.RemoveMeasureEvent.newBuilder()
                                        .setId(key.getSensor().getId())//
                                        .setMeasure(value)//
                                )
                                .build());

                    })//
                    .build(key -> convertMeasure(measureRepository.getOne(key)).build());
        }

        private Observable<SensorProjectionEvents.SensorProjectionEvent> getStream() {
            return getCurrentState().toObservable().concatWith(eventSubject);
        }

        void handle(AquabianEvents.DeviceCreatedEvent event) {

        }

        void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
        }

        void handle(AquabianEvents.SensorCreatedEvent event) {
            final SensorProjectionEvents.Sensor sensor = SensorProjectionEvents.Sensor.newBuilder()//
                    .setId(event.getId())//
                    .setName(event.getName())//
                    .setDeviceId(event.getId())//
                    .build();
            eventSubject.onNext(SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                    .setAddSensorEvent(SensorProjectionEvents.AddSensorEvent.newBuilder()//
                            .setSensor(sensor)//
                    ).build());

        }

        void handle(AquabianEvents.MeasureAddedEvent event) {
            SensorEntity sensorEntity = new SensorEntity();
            sensorEntity.setId(event.getId());
            SensorProjectionEvents.Measure measure = SensorProjectionEvents.Measure.newBuilder()//
                    .setValue(event.getValue())//
                    .setDate(event.getDate())//
                    .build();
            Instant date = ProtoUtils.convertTimestampProtoToInstant(event.getDate());
            measures.put(new MeasurePrimaryKey(sensorEntity, date), measure);
            eventSubject.onNext(SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                    .setAddMeasureEvent(SensorProjectionEvents.AddMeasureEvent.newBuilder()//
                            .setId(event.getId())//
                            .setMeasure(measure)//
                    ).build());

        }


        private Single<SensorProjectionEvents.SensorProjectionEvent> getCurrentState() {
            return Observable.fromCallable(() -> measureRepository.findByDateAfter(Instant.now().minusSeconds(second)))//
                    .subscribeOn(Schedulers.io())//
                    .flatMap(Observable::fromIterable)//
                    .groupBy(MeasureEntity::getSensor)//
                    .flatMapSingle(obs -> obs.map(this::convertMeasure)//
                            .map(SensorProjectionEvents.Measure.Builder::build)//
                            .doOnNext(measure -> measures.put(new MeasurePrimaryKey(obs.getKey(), ProtoUtils.convertTimestampProtoToInstant(measure.getDate())), measure))
                            .toList()//
                            .map(m -> convertSensor(obs.getKey()).addAllMeasures(m).build()))//
                    .toList()//
                    .map(sensors -> SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                            .setCurrentStateEvent(SensorProjectionEvents.CurrentStateEvent.newBuilder()//
                                    .addAllSensors(sensors)//
                            ).build());
        }


        private SensorProjectionEvents.Sensor.Builder convertSensor(SensorEntity entity) {
            return SensorProjectionEvents.Sensor.newBuilder()//
                    .setId(entity.getId())//
                    .setDeviceId(entity.getDevice().getId())//
                    .setName(entity.getName());

        }

        private SensorProjectionEvents.Measure.Builder convertMeasure(MeasureEntity entity) {
            return SensorProjectionEvents.Measure.newBuilder()//
                    .setValue(entity.getValue())//
                    .setDate(Timestamps.fromMillis(entity.getDate().toEpochMilli()));//

        }
    }
}

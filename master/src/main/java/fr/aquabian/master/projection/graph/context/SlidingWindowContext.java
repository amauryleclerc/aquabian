package fr.aquabian.master.projection.graph.context;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import fr.aquabian.master.projection.graph.GraphUtils;
import fr.aquabian.master.projection.persistence.entity.MeasurePrimaryKey;
import fr.aquabian.master.projection.persistence.entity.SensorEntity;
import fr.aquabian.master.projection.persistence.repository.MeasureRepository;
import fr.aquabian.toolkit.ProtoUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class SlidingWindowContext implements IGraphContext {

    private final MeasureRepository measureRepository;
    private final long second;
    private Subject<SensorProjectionEvents.SensorProjectionEvent> eventSubject = PublishSubject.<SensorProjectionEvents.SensorProjectionEvent>create().toSerialized();
    private LoadingCache<MeasurePrimaryKey, SensorProjectionEvents.Measure> measures;

    public SlidingWindowContext(long second, MeasureRepository measureRepository) {
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
                .build(key -> GraphUtils.convertMeasure(measureRepository.getOne(key)).build());
    }

    @Override
    public Observable<SensorProjectionEvents.SensorProjectionEvent> getStream() {
        return getCurrentState().toObservable().concatWith(eventSubject);
    }


    @Override
    public void handle(AquabianEvents.SensorCreatedEvent event) {
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

    @Override
    public void handle(AquabianEvents.MeasureAddedEvent event) {
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
    @Override
    public void handle(AquabianEvents.SensorRenamedEvent event) {
        if(measures.asMap().keySet().stream().map(k -> k.getSensor().getId()).anyMatch(k -> event.getId().equals(event.getId()))){
            eventSubject.onNext(SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                    .setSensorRenamedEvent(AquabianEvents.SensorRenamedEvent.newBuilder()//
                            .setId(event.getId())//
                            .setName(event.getName())//
                    ).build());
        }
    }
    private Single<SensorProjectionEvents.SensorProjectionEvent> getCurrentState() {
        return GraphUtils.getCurrentState(() -> measureRepository.findByDateAfter(Instant.now().minusSeconds(second)),//
                (s, m) -> measures.put(new MeasurePrimaryKey(s, ProtoUtils.convertTimestampProtoToInstant(m.getDate())), m));//
    }

}

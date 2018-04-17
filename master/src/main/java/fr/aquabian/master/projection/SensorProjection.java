package fr.aquabian.master.projection;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.protobuf.Timestamp;
import fr.aquabian.api.ISensorProjectionEventStream;
import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@ProcessingGroup("Projection")
public class SensorProjection implements ISensorProjectionEventStream {

    private Map<String, SensorProjectionEvents.Sensor> sensorMap = new HashMap<>();
    private Table<String, Timestamp, Double> measureMap = HashBasedTable.create();
    private Subject<SensorProjectionEvents.SensorProjectionEvent> sensorProjectionEventSubject = PublishSubject.<SensorProjectionEvents.SensorProjectionEvent>create().toSerialized();


    @EventSourcingHandler
    public void handle(AquabianEvents.SensorCreatedEvent event) {
        final SensorProjectionEvents.Sensor sensor = SensorProjectionEvents.Sensor.newBuilder()//
                .setId(event.getId())//
                .setName(event.getName())//
                .setDeviceId(event.getId())//
                .build();
        sensorMap.put(event.getId(), sensor);
        sensorProjectionEventSubject.onNext(SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                .setAddSensorEvent(SensorProjectionEvents.AddSensorEvent.newBuilder()//
                        .setSensors(sensor)//
                ).build());

    }

    @EventSourcingHandler
    public void handle(AquabianEvents.MeasureAddedEvent event) {
        measureMap.put(event.getId(),event.getDate(), event.getValue());
        sensorProjectionEventSubject.onNext(SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                .setAddMeasureEvent(SensorProjectionEvents.AddMeasureEvent.newBuilder()//
                        .setId(event.getId())//
                        .setMeasures(SensorProjectionEvents.Measure.newBuilder()//
                                .setValue(event.getValue())//
                                .setDate(event.getDate())//
                        )
                ).build());
    }


    @Override
    public Observable<SensorProjectionEvents.SensorProjectionEvent> getStream() {
        return Observable.fromCallable(() ->
                SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                        .setCurrentStateEvent(SensorProjectionEvents.CurrentStateEvent.newBuilder()
                                .addAllSensors(sensorMap.values())//
                        ).build())//
                .concatWith(sensorProjectionEventSubject);
    }
}

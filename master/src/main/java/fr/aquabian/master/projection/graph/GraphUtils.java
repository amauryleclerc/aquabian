package fr.aquabian.master.projection.graph;

import com.google.protobuf.util.Timestamps;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import fr.aquabian.master.projection.persistence.entity.MeasureEntity;
import fr.aquabian.master.projection.persistence.entity.SensorEntity;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GraphUtils {

    private GraphUtils() {

    }

    public static Single<SensorProjectionEvents.SensorProjectionEvent> getCurrentState(Supplier<Collection<MeasureEntity>> sensorSupplier) {
        return getCurrentState(sensorSupplier, (s, m) -> {
        });
    }

    public static Single<SensorProjectionEvents.SensorProjectionEvent> getCurrentState(Supplier<Collection<MeasureEntity>> sensorSupplier, BiConsumer<SensorEntity, SensorProjectionEvents.Measure> measureConsumer) {
        return Observable.fromCallable(sensorSupplier::get)//
                .subscribeOn(Schedulers.io())//
                .flatMap(Observable::fromIterable)//
                .groupBy(MeasureEntity::getSensor)//
                .flatMapSingle(obs -> obs.map(GraphUtils::convertMeasure)//
                        .map(SensorProjectionEvents.Measure.Builder::build)//
                        .doOnNext(measure -> measureConsumer.accept(obs.getKey(), measure))//
                        .toList()//
                        .map(m -> convertSensor(obs.getKey()).addAllMeasures(m).build()))//
                .toList()//
                .map(sensors -> SensorProjectionEvents.SensorProjectionEvent.newBuilder()//
                        .setCurrentStateEvent(SensorProjectionEvents.CurrentStateEvent.newBuilder()//
                                .addAllSensors(sensors)//
                        ).build());
    }

    public static SensorProjectionEvents.Sensor.Builder convertSensor(SensorEntity entity) {
        return SensorProjectionEvents.Sensor.newBuilder()//
                .setId(entity.getId())//
                .setDeviceId(entity.getDevice().getId())//
                .setName(entity.getName());

    }

    public static SensorProjectionEvents.Measure.Builder convertMeasure(MeasureEntity entity) {
        return SensorProjectionEvents.Measure.newBuilder()//
                .setValue(entity.getValue())//
                .setDate(Timestamps.fromMillis(entity.getDate().toEpochMilli()));//

    }
}

package fr.aquabian.slave.service;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SensorManager implements ISensorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorManager.class);

    public static final String DEVICES_PATH = "/sys/bus/w1/devices";

    private Subject<ISensor.Measure> measureSubject = PublishSubject.<ISensor.Measure>create().toSerialized();

    private final Collection<ISensor> sensors;

    public SensorManager() {
        sensors = findSensors();
        if (sensors.isEmpty()) {
            LOGGER.warn("No sensor found");
            sensors.add(new MockSensor());
        }
    }


    private Collection<ISensor> findSensors() {
        return Optional.of(Paths.get(SensorManager.DEVICES_PATH))//
                .map(Path::toFile)//
                .filter(File::exists)//
                .filter(File::isDirectory)//
                .flatMap(d -> Optional.ofNullable(d.listFiles((f, n) -> n.startsWith("28-"))))//
                .map(Stream::of)//
                .orElse(Stream.empty())//
                .map(File::getName)//
                .map(Sensor::new)//
                .collect(Collectors.toSet());
    }


    @Override
    public Collection<ISensor> getSensors() {
        return Collections.unmodifiableCollection(sensors);
    }
}

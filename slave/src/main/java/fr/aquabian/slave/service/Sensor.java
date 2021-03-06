package fr.aquabian.slave.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Sensor implements ISensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sensor.class);

    private final String sensorId;


    public Sensor(String sensorId) {
        this.sensorId = sensorId;

    }

    @Override
    public Optional<Measure> getMeasure() {
        return Optional.of(Paths.get(SensorManager.DEVICES_PATH).resolve(sensorId))//
                .map(Path::toFile)//
                .flatMap(d -> Optional.ofNullable(d.listFiles((f, n) -> n.equals("w1_slave"))))//
                .map(Stream::of)//
                .orElse(Stream.empty())//
                .filter(File::isFile)//
                .flatMap(f -> {
                    try {
                        return Files.lines(f.toPath());
                    } catch (IOException e) {
                        LOGGER.error("Cannot read file", e);
                        throw new RuntimeException(e);
                    }
                })//
                .filter(l -> l.contains("t="))//
                .map(l -> l.substring(l.indexOf("t=") + 2))//
                .map(Double::valueOf)//
                .map(t -> t / 1000d)//
                .findAny()
                .map(v -> new Measure(sensorId,v,Instant.now()));
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }
}

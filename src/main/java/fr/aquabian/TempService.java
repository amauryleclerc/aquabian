package fr.aquabian;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class TempService {

    private static final String DEVICES_PATH = "/sys/bus/w1/devices";


    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);


    public Optional<Double> getTemp() {
        File directory = Paths.get(DEVICES_PATH).toFile();
        if (!directory.exists()) {
            LOGGER.error("Directory {} not found", DEVICES_PATH);
            return Optional.empty();
        }
        if (!directory.isDirectory()) {
            LOGGER.error("{} is not a directory", DEVICES_PATH);
            return Optional.empty();
        }
        return Stream.of(Objects.requireNonNull(directory.listFiles()))//
                .filter(File::isDirectory)//
                .filter(f -> f.getName().startsWith("28-"))//
                .flatMap(d -> Stream.of(Objects.requireNonNull(d.listFiles((f, n) -> n.equals("w1_slave")))))//
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
                .findAny();
    }
}

package fr.aquabian.slave.service;

import java.time.Instant;
import java.util.Optional;

public interface ISensor {

    /**
     * Find measure
     * @return
     */
    Optional<Measure> getMeasure();



    /**
     * sensor Id
     * @return
     */
    String getSensorId();


    class Measure {
        private final String sensorId;

        private final double value;

        private final Instant date;

        public Measure(String sensorId, double value, Instant date) {
            this.sensorId = sensorId;
            this.value = value;
            this.date = date;
        }

        public String getSensorId() {
            return sensorId;
        }

        public double getValue() {
            return value;
        }

        public Instant getDate() {
            return date;
        }
    }
}

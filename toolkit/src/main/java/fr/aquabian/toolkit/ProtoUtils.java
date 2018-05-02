package fr.aquabian.toolkit;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class ProtoUtils {
    private ProtoUtils() {

    }

    public static Instant convertTimestampProtoToInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    public static Timestamp convertInstantToTimestampProto(Instant instant) {
        return Timestamp.newBuilder()//
                .setSeconds(instant.getEpochSecond())//
                .setNanos(instant.getNano())
                .build();
    }
}

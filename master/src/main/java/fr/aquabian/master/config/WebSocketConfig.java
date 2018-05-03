package fr.aquabian.master.config;


import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import fr.aquabian.api.AquabianConstants;
import fr.aquabian.api.ISensorProjectionEventStream;
import fr.aquabian.api.projection.command.SensorProjectionEvents;
import fr.aquabian.master.handle.ProtoWebSocketHandler;
import fr.aquabian.master.projection.graph.IGraphProjection;
import fr.aquabian.toolkit.ProtoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ISensorProjectionEventStream eventStream;
    private final IGraphProjection graphProjection;

    @Autowired
    public WebSocketConfig(final ISensorProjectionEventStream eventStream, IGraphProjection graphProjection) {
        this.eventStream = eventStream;
        this.graphProjection = graphProjection;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ProtoWebSocketHandler.create(m -> {
                    SensorProjectionEvents.GraphQuery.Builder query = SensorProjectionEvents.GraphQuery.newBuilder();
                    getValue(m, "afterglow")//
                            .ifPresent(sec -> query.setSlidingWindowQuery(SensorProjectionEvents.SlidingWindowQuery.newBuilder()//
                                    .setAfterglowSec(sec)));
                    getValue(m, "dateMin")//
                            .flatMap(dateMin -> getValue(m, "dateMax")//
                                    .map(dateMax -> SensorProjectionEvents.PastWindowQuery.newBuilder()//
                                            .setDateMax(Timestamps.fromMillis(dateMax))//
                                            .setDateMin(Timestamps.fromMillis(dateMin))))//
                            .ifPresent(query::setPastWindowQuery);
                    return graphProjection.getStream(query.build());
                }),//
                AquabianConstants.SENSOR_PROJECTION_EVENT_STREAM_PATH);
    }


    private Optional<Long> getValue(Map<String, List<String>> map, String key) {
        return Optional.ofNullable(map.get(key))//
                .filter(l -> l.size() > 0)//
                .map(l -> l.get(0))//
                .flatMap(v -> {
                    try {
                        return Optional.of(Long.valueOf(v));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                });//
    }


}

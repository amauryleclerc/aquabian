package fr.aquabian.master.config;


import fr.aquabian.api.AquabianConstants;
import fr.aquabian.api.ISensorProjectionEventStream;
import fr.aquabian.master.handle.ProtoWebSocketHandler;
import fr.aquabian.master.projection.graph.IGraphProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

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
                    long second = Optional.ofNullable(m.get("seconds"))//
                            .filter(l -> l.size()>0)//
                            .map(l -> l.get(0))//
                            .map(Long::valueOf)//
                            .orElse(30l);
                    return graphProjection.getStream(second);
                }),//
                AquabianConstants.SENSOR_PROJECTION_EVENT_STREAM_PATH);
    }


}

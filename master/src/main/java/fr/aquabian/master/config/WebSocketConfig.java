package fr.aquabian.master.config;


import fr.aquabian.api.AquabianConstants;
import fr.aquabian.api.ISensorProjectionEventStream;
import fr.aquabian.master.handle.ProtoWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ISensorProjectionEventStream eventStream;

    @Autowired
    public WebSocketConfig(final ISensorProjectionEventStream eventStream) {
        this.eventStream = eventStream;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ProtoWebSocketHandler.create(eventStream::getStream), AquabianConstants.SENSOR_PROJECTION_EVENT_STREAM_PATH);
    }


}

package fr.aquabian.master.handle;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import fr.aquabian.toolkit.RxUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ProtoWebSocketHandler implements WebSocketHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProtoWebSocketHandler.class);
    private final Supplier<Observable<? extends Message>> streamSupplier;

    private final Map<String, Disposable> subs = new HashMap<>();

    private ProtoWebSocketHandler(Supplier<Observable<? extends Message>> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    public static ProtoWebSocketHandler create(Supplier<Observable<? extends Message>> streamSupplier) {
        return new ProtoWebSocketHandler(streamSupplier);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        LOGGER.info("New webwork session {} - {}", webSocketSession.getId(), webSocketSession.getPrincipal());
        Disposable sub = streamSupplier.get()
                .map(MessageLite::toByteArray)//
                .map(BinaryMessage::new)//
                .doOnTerminate(webSocketSession::close)
                .subscribe(webSocketSession::sendMessage, RxUtils.logError(LOGGER));
        subs.put(webSocketSession.getId(), sub);


    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        LOGGER.info("Handle message {}", webSocketMessage);
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        LOGGER.error("WebSocket error", throwable);
        dispose(webSocketSession.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        dispose(webSocketSession.getId());
    }

    private void dispose(String id) {
        Optional.ofNullable(subs.get(id)).ifPresent(Disposable::dispose);
    }


    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

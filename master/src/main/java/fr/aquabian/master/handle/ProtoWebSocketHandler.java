package fr.aquabian.master.handle;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import fr.aquabian.toolkit.RxUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ProtoWebSocketHandler implements WebSocketHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProtoWebSocketHandler.class);
    private final Function<Map<String, List<String>>, Observable<? extends Message>> streamSupplier;

    private final Map<String, Disposable> subs = new HashMap<>();

    private ProtoWebSocketHandler(Function<Map<String, List<String>>, Observable<? extends Message>> streamSupplier) {
        this.streamSupplier = streamSupplier;
    }

    public static ProtoWebSocketHandler create(Function<Map<String, List<String>>, Observable<? extends Message>> streamSupplier) {
        return new ProtoWebSocketHandler(streamSupplier);
    }

    public static ProtoWebSocketHandler create(Supplier<Observable<? extends Message>> streamSupplier) {
        return new ProtoWebSocketHandler(m -> streamSupplier.get());
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        LOGGER.info("New webwork session {} - {}", webSocketSession.getId(), webSocketSession.getPrincipal());
        Disposable sub = streamSupplier.apply(splitQuery(webSocketSession.getUri()))//
                .observeOn(Schedulers.io())//
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

    private Map<String, List<String>> splitQuery(URI url) {
        if (Strings.isNullOrEmpty(url.getQuery())) {
            return Collections.emptyMap();
        }
        return Arrays.stream(url.getQuery().split("&"))
                .map(this::splitQueryParameter)
                .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, toList())));
    }

    private AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }
}

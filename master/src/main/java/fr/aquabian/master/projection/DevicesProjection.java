package fr.aquabian.master.projection;

import fr.aquabian.api.domain.event.AquabianEvents;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ProcessingGroup("Projection")
public class DevicesProjection implements IDeviceService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DevicesProjection.class);

    private Map<String, List<String>> deviceMap = new HashMap<>();

    @EventHandler
    public void handle(AquabianEvents.DeviceCreatedEvent event) {
        LOGGER.debug("Event received DeviceCreatedEvent : {}", event);
        deviceMap.put(event.getId(), new ArrayList<>(event.getSensorsList()));
    }

    @EventHandler
    public void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
        LOGGER.debug("Event received SensorAddedToDeviceEvent : {}", event);
        deviceMap.get(event.getId()).add(event.getIdSensor());
    }


    @Override
    public boolean isExist(String deviceId) {
        return deviceMap.containsKey(deviceId);
    }

    @Override
    public Collection<String> getSensors(String deviceId) {
        return Optional.ofNullable(deviceMap.get(deviceId)).orElse(Collections.emptyList());
    }
}
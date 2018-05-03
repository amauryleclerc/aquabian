package fr.aquabian.master.projection.persistence;

import fr.aquabian.api.domain.event.AquabianEvents;
import fr.aquabian.master.projection.persistence.entity.DeviceEntity;
import fr.aquabian.master.projection.persistence.entity.MeasureEntity;
import fr.aquabian.master.projection.persistence.entity.SensorEntity;
import fr.aquabian.master.projection.persistence.repository.DeviceRepository;
import fr.aquabian.master.projection.persistence.repository.MeasureRepository;
import fr.aquabian.master.projection.persistence.repository.SensorRepository;
import fr.aquabian.toolkit.ProtoUtils;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@ProcessingGroup("PersistenceProjection")
public class PersisitenceProjection {


    private final static Logger LOGGER = LoggerFactory.getLogger(PersisitenceProjection.class);
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final MeasureRepository measureRepository;

    @Autowired
    private PersisitenceProjection(final DeviceRepository deviceRepository,//
                                   final SensorRepository sensorRepository,//
                                   final MeasureRepository measureRepository) {
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.measureRepository = measureRepository;
    }

    @EventHandler
    public void handle(AquabianEvents.DeviceCreatedEvent event) {
        LOGGER.info("Create device {} with name {}", event.getId(), event.getName());
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setId(event.getId());
        deviceEntity.setName(event.getName());
        deviceRepository.saveAndFlush(deviceEntity);
    }

    @EventHandler
    public void handle(AquabianEvents.SensorAddedToDeviceEvent event) {
        LOGGER.info("Add sensor {} to device {}", event.getIdSensor(), event.getId());
        DeviceEntity device = deviceRepository.getOne(event.getId());
        SensorEntity sensor = sensorRepository.getOne(event.getIdSensor());
        device.getSensors().add(sensor);
        deviceRepository.saveAndFlush(device);
    }

    @EventHandler
    public void handle(AquabianEvents.SensorCreatedEvent event) {
        LOGGER.info("Create sensor {} with name {}", event.getId(), event.getName());
        DeviceEntity device = deviceRepository.getOne(event.getDevice());
        SensorEntity sensor = new SensorEntity();
        sensor.setId(event.getId());
        sensor.setName(event.getName());
        sensor.setDevice(device);
        sensorRepository.saveAndFlush(sensor);
    }

    @EventHandler
    public void handle(AquabianEvents.MeasureAddedEvent event) {
        LOGGER.info("Add mesure {} from sensor {} at {}", event.getValue(), event.getId(), ProtoUtils.convertTimestampProtoToInstant(event.getDate()));
        SensorEntity sensor = sensorRepository.getOne(event.getId());
        MeasureEntity measure = new MeasureEntity();
        measure.setDate(Instant.ofEpochSecond(event.getDate().getSeconds(), event.getDate().getNanos()));
        measure.setSensor(sensor);
        measure.setValue(event.getValue());
        measureRepository.saveAndFlush(measure);
    }


}

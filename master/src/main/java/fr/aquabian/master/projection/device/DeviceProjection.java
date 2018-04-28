package fr.aquabian.master.projection.device;

import fr.aquabian.master.projection.persistence.entity.SensorEntity;
import fr.aquabian.master.projection.persistence.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class DeviceProjection implements IDeviceService {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceProjection(DeviceRepository deviceRepository){
        this.deviceRepository = deviceRepository;
    }

    @Override
    public boolean isExist(String deviceId) {
        return deviceRepository.findById(deviceId).isPresent();
    }

    @Override
    public Collection<String> getSensors(String deviceId) {
        return deviceRepository.findById(deviceId)//
                .map(d -> d.getSensors()//
                        .stream()//
                        .map(SensorEntity::getId)//
                        .collect(Collectors.toSet())//
                ).orElseGet(Collections::emptySet);
    }
}

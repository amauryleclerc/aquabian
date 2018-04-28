package fr.aquabian.master.projection.device;

import java.util.Collection;

public interface IDeviceService {

    boolean isExist(String deviceId);

    Collection<String> getSensors(String deviceId);

}

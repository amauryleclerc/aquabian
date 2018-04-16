package fr.aquabian.master.projection;

import java.util.Collection;

public interface IDeviceService {

    boolean isExist(String deviceId);

    Collection<String> getSensors(String deviceId);

}

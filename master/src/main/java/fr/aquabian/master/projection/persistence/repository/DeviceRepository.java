package fr.aquabian.master.projection.persistence.repository;

import fr.aquabian.master.projection.persistence.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository  extends JpaRepository<DeviceEntity,String> {
}

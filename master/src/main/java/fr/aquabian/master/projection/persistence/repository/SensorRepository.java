package fr.aquabian.master.projection.persistence.repository;

import fr.aquabian.master.projection.persistence.entity.SensorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<SensorEntity,String> {
}

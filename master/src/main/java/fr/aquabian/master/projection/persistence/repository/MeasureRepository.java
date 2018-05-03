package fr.aquabian.master.projection.persistence.repository;

import fr.aquabian.master.projection.persistence.entity.MeasureEntity;
import fr.aquabian.master.projection.persistence.entity.MeasurePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MeasureRepository extends JpaRepository<MeasureEntity, MeasurePrimaryKey> {

    List<MeasureEntity> findByDateAfter(Instant date);

    List<MeasureEntity> findByDateBetween(Instant dateMin, Instant dateMax);
}

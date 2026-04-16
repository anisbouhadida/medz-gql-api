package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NomenclatureEventRepository
    extends JpaRepository<NomenclatureEventEntity, Long> {

  Optional<NomenclatureEventEntity> findByMedicineId(Long medicineId);
}


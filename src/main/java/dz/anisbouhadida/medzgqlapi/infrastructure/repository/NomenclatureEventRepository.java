package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NomenclatureEventRepository
    extends JpaRepository<NomenclatureEventEntity, Long> {

  Optional<NomenclatureEventEntity> findByMedicineId(Long medicineId);

  List<NomenclatureEventEntity> findByMedicineIdIn(List<Long> medicineIds);
}

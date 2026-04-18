package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NonRenewalEventRepository extends JpaRepository<NonRenewalEventEntity, Long> {

  Optional<NonRenewalEventEntity> findByMedicineId(Long medicineId);

  List<NonRenewalEventEntity> findByMedicineIdIn(List<Long> medicineIds);
}

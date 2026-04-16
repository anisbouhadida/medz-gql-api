package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NonRenewalEventRepository extends JpaRepository<NonRenewalEventEntity, Long> {

  Optional<NonRenewalEventEntity> findByMedicineId(Long medicineId);
}


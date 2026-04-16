package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalEventRepository extends JpaRepository<WithdrawalEventEntity, Long> {

  Optional<WithdrawalEventEntity> findByMedicineId(Long medicineId);
}


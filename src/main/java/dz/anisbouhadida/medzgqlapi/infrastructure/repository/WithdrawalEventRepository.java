package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WithdrawalEventRepository extends JpaRepository<WithdrawalEventEntity, Long> {

  Optional<WithdrawalEventEntity> findByMedicineId(Long medicineId);

  List<WithdrawalEventEntity> findByMedicineIdIn(List<Long> medicineIds);
}

package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineStatusHistoryRepository
    extends JpaRepository<MedicineStatusHistoryEntity, MedicineStatusHistoryId> {

  List<MedicineStatusHistoryEntity> findByMedicineIdOrderByStatusTimestampDesc(Long medicineId);

  List<MedicineStatusHistoryEntity> findByStatus(MedicineStatus status);

  /// Returns the IDs of medicines whose **current** (most recent) status
  /// matches the given value.
  @Query("""
      SELECT sh.medicineId FROM MedicineStatusHistoryEntity sh
       WHERE sh.status = :status
         AND sh.statusTimestamp = (
             SELECT MAX(sh2.statusTimestamp)
               FROM MedicineStatusHistoryEntity sh2
              WHERE sh2.medicineId = sh.medicineId
         )
      """)
  List<Long> findMedicineIdsByCurrentStatus(@Param("status") MedicineStatus status);
}


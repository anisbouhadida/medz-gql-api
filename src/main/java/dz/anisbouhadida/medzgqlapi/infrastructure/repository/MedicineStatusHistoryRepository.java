package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicineStatusHistoryRepository
    extends JpaRepository<MedicineStatusHistoryEntity, MedicineStatusHistoryId> {

  List<MedicineStatusHistoryEntity> findByMedicineIdOrderByStatusTimestampDesc(Long medicineId);

  List<MedicineStatusHistoryEntity> findByStatus(MedicineStatus status);

  /// Returns the IDs of medicines whose **current** (most recent) status
  /// matches the given value.
  /// @param status the status to filter by
  /// @return a list of medicine IDs whose current status matches the given value
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

  /// Returns the latest status history entry for each of the given medicine IDs.
  /// @param medicineIds the medicine IDs to look up
  /// @return a list of the latest status history entries for the given medicine IDs
  @Query("""
      SELECT sh FROM MedicineStatusHistoryEntity sh
       WHERE sh.medicineId IN :medicineIds
         AND sh.statusTimestamp = (
             SELECT MAX(sh2.statusTimestamp)
               FROM MedicineStatusHistoryEntity sh2
              WHERE sh2.medicineId = sh.medicineId
         )
      """)
  List<MedicineStatusHistoryEntity> findLatestByMedicineIds(@Param("medicineIds") List<Long> medicineIds);
}


package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEventHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEventHistoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineEventHistoryRepository
    extends JpaRepository<MedicineEventHistoryEntity, MedicineEventHistoryId> {

  List<MedicineEventHistoryEntity> findByMedicineIdOrderByUpdatedAtDesc(Long medicineId);

  List<MedicineEventHistoryEntity> findByEventType(MedicineEventType eventType);
}


package dz.anisbouhadida.medzgqlapi.infrastructure.adapter;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineType;
import dz.anisbouhadida.medzgqlapi.domain.spi.MedicineSpi;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.mapper.MedicineMapper;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.MedicineRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.MedicineStatusHistoryRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.NomenclatureEventRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.NonRenewalEventRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.WithdrawalEventRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/// Infrastructure adapter that implements [MedicineSpi] using
/// Spring Data JPA repositories.
///
/// All methods run inside a read-only transaction so that lazy-loaded
/// associations remain accessible during entity-to-domain mapping.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineAdapter implements MedicineSpi {

  private final MedicineRepository medicineRepository;
  private final MedicineStatusHistoryRepository statusHistoryRepository;
  private final NomenclatureEventRepository nomenclatureEventRepository;
  private final WithdrawalEventRepository withdrawalEventRepository;
  private final NonRenewalEventRepository nonRenewalEventRepository;
  private final MedicineMapper mapper;

  @Override
  public Optional<Medicine> findByRegistrationNumber(String registrationNumber) {
    return medicineRepository
        .findFirstByRegistrationNumber(registrationNumber)
        .map(mapper::toDomain);
  }

  @Override
  public Optional<Medicine> findByCode(String code) {
    return medicineRepository.findFirstByCode(code).map(mapper::toDomain);
  }

  @Override
  public List<Medicine> findAll(
      MedicineType type, MedicineOrigin origin, MedicineStatus status) {

    Specification<MedicineEntity> spec =
        (root, query, cb) -> cb.conjunction();

    if (type != null) {
      spec = spec.and((root, _, cb) -> cb.equal(root.get("type"), type));
    }
    if (origin != null) {
      spec = spec.and((root, _, cb) -> cb.equal(root.get("origin"), origin));
    }
    if (status != null) {
      List<Long> medicineIds = statusHistoryRepository.findMedicineIdsByCurrentStatus(status);
      if (medicineIds.isEmpty()) {
        return List.of();
      }
      spec = spec.and((root, _, _) -> root.get("medicineId").in(medicineIds));
    }

    return medicineRepository.findAll(spec).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<MedicineEvent> findEventsByRegistrationNumber(String registrationNumber) {
    List<MedicineEntity> medicines =
        medicineRepository.findByRegistrationNumber(registrationNumber);
    List<MedicineEvent> events = new ArrayList<>();

    for (MedicineEntity medicineEntity : medicines) {
      Long id = medicineEntity.getMedicineId();
      Medicine medicine = mapper.toDomain(medicineEntity);

      nomenclatureEventRepository
          .findByMedicineId(id)
          .map(e -> mapper.toDomain(e, medicine))
          .ifPresent(events::add);

      withdrawalEventRepository
          .findByMedicineId(id)
          .map(e -> mapper.toDomain(e, medicine))
          .ifPresent(events::add);

      nonRenewalEventRepository
          .findByMedicineId(id)
          .map(e -> mapper.toDomain(e, medicine))
          .ifPresent(events::add);
    }

    return events;
  }
}



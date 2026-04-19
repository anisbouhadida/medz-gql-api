package dz.anisbouhadida.medzgqlapi.infrastructure.adapter;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.spi.MedicineSpi;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.mapper.MedicineMapper;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        .findByRegistrationNumber(registrationNumber)
        .map(mapper::toDomain);
  }

  @Override
  public List<Medicine> findByCode(String code) {
    return medicineRepository.findByCode(code).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Medicine> findByIcd(String icd) {
    return medicineRepository.findByIcd(icd).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Medicine> findByBrandName(String brandName) {
    return medicineRepository.findByBrandName(brandName).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Medicine> findByLaboratoryHolder(String laboratoryHolder) {
    return medicineRepository.findByLaboratoryHolder(laboratoryHolder).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Medicine> search(MedicineSearchFilter filter) {

    String searchText = filter.searchText();
    MedicineOrigin origin = filter.origin();
    MedicineStatus status = filter.status();
    List<String> laboratoryHolders = filter.laboratoryHolders();

    Specification<MedicineEntity> spec = (root, query, cb) -> cb.conjunction();

    if (searchText != null && !searchText.isBlank()) {
      String escaped = escapeLike(searchText.toLowerCase());
      String pattern = "%" + escaped + "%";
      spec =
          spec.and(
              (root, _, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("registrationNumber")), pattern),
                      cb.like(cb.lower(root.get("code")), pattern),
                      cb.like(cb.lower(root.get("icd")), pattern),
                      cb.like(cb.lower(root.get("brandName")), pattern)));
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
    if (laboratoryHolders != null && !laboratoryHolders.isEmpty()) {
      spec = spec.and((root, _, cb) -> cb.lower(root.get("laboratoryHolder")).in(
          laboratoryHolders.stream().map(String::toLowerCase).toList()));
    }

    return medicineRepository.findAll(spec).stream().map(mapper::toDomain).toList();
  }

  /// Escapes SQL LIKE special characters (`%`, `_`, `\`) so that
  /// user input is treated as literal text.
  private static String escapeLike(String input) {
    return input
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
  }

  @Override
  public Map<Long, MedicineStatus> findLatestStatusByMedicineIds(List<Long> medicineIds) {
    if (medicineIds.isEmpty()) {
      return Map.of();
    }
    return statusHistoryRepository.findLatestByMedicineIds(medicineIds).stream()
        .collect(Collectors.toMap(
            MedicineStatusHistoryEntity::getMedicineId,
            MedicineStatusHistoryEntity::getStatus
        ));
  }

  @Override
  public Map<Long, List<MedicineEvent>> findEventsByMedicineIds(List<Long> medicineIds) {
    if (medicineIds.isEmpty()) {
      return Map.of();
    }

    Map<Long, List<MedicineEvent>> result = medicineIds.stream()
        .collect(Collectors.toMap(id -> id, _ -> new ArrayList<>()));

    nomenclatureEventRepository.findByMedicineIdIn(medicineIds)
        .forEach(e -> result.get(e.getMedicineId()).add(mapper.toDomain(e)));

    withdrawalEventRepository.findByMedicineIdIn(medicineIds)
        .forEach(e -> result.get(e.getMedicineId()).add(mapper.toDomain(e)));

    nonRenewalEventRepository.findByMedicineIdIn(medicineIds)
        .forEach(e -> result.get(e.getMedicineId()).add(mapper.toDomain(e)));

    return result;
  }
}



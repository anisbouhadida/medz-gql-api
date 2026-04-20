package dz.anisbouhadida.medzgqlapi.infrastructure.adapter;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePage;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePageRequest;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSortInput;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineSortField;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.SortDirection;
import dz.anisbouhadida.medzgqlapi.domain.spi.MedicineSpi;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.mapper.MedicineMapper;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public MedicinePage search(MedicineSearchFilter filter, MedicinePageRequest pageRequest) {

    String searchText = filter.searchText();
    MedicineOrigin origin = filter.origin();
    MedicineStatus status = filter.status();
    List<String> laboratoryHolders = filter.laboratoryHolders();

    Specification<MedicineEntity> spec = (_, _, cb) -> cb.conjunction();

    if (searchText != null && !searchText.isBlank()) {
      String escaped = escapeLike(searchText.toLowerCase());
      String pattern = "%" + escaped + "%";
      spec =
          spec.and(
              (root, _, cb) ->
                  cb.or(
                      cb.like(cb.lower(root.get("registrationNumber")), pattern, '\\'),
                      cb.like(cb.lower(root.get("code")), pattern, '\\'),
                      cb.like(cb.lower(root.get("icd")), pattern, '\\'),
                      cb.like(cb.lower(root.get("brandName")), pattern, '\\')));
    }
    if (origin != null) {
      spec = spec.and((root, _, cb) -> cb.equal(root.get("origin"), origin));
    }
    if (status != null) {
      List<Long> medicineIds = statusHistoryRepository.findMedicineIdsByCurrentStatus(status);
      if (medicineIds.isEmpty()) {
        return new MedicinePage(List.of(), 0L, false, false, 0L);
      }
      spec = spec.and((root, _, _) -> root.get("medicineId").in(medicineIds));
    }
    if (laboratoryHolders != null && !laboratoryHolders.isEmpty()) {
      spec = spec.and((root, _, cb) -> cb.lower(root.get("laboratoryHolder")).in(
          laboratoryHolders.stream().map(String::toLowerCase).toList()));
    }

    Pageable pageable = buildPageable(pageRequest);
    Page<MedicineEntity> page = medicineRepository.findAll(spec, pageable);

    List<Medicine> content = page.getContent().stream().map(mapper::toDomain).toList();
    long startOffset = pageable.getOffset();

    return new MedicinePage(
        content,
        page.getTotalElements(),
        page.hasNext(),
        page.hasPrevious(),
        startOffset);
  }

  /// Builds a Spring Data [Pageable] from the domain [MedicinePageRequest].
  ///
  /// Forward pagination uses first/after; backward uses last/before.
  /// When neither is provided, defaults to the first page of DEFAULT_PAGE_SIZE.
  private static Pageable buildPageable(MedicinePageRequest req) {
    Sort sort = buildSort(req.sort());

    boolean backward = req.last() != null || req.before() != null;

    if (backward) {
      int pageSize = req.last() != null ? req.last() : MedicinePageRequest.DEFAULT_PAGE_SIZE;
      long beforeOffset = req.before() != null
          ? decodeCursor(req.before())
          : Long.MAX_VALUE;
      // Start of the page = beforeOffset - pageSize (clamped to 0)
      long startOffset = Math.max(0L, beforeOffset - pageSize);
      int pageNumber = (int) (startOffset / pageSize);
      return PageRequest.of(pageNumber, pageSize, sort);
    }

    int pageSize = req.first() != null ? req.first() : MedicinePageRequest.DEFAULT_PAGE_SIZE;
    long afterOffset = req.after() != null ? decodeCursor(req.after()) : -1L;
    long startOffset = afterOffset + 1;
    int pageNumber = (int) (startOffset / pageSize);
    return PageRequest.of(pageNumber, pageSize, sort);
  }

  /// Decodes an opaque base64 cursor (format {@code "offset:<n>"}) to its absolute offset.
  private static long decodeCursor(String cursor) {
    try {
      String raw = new String(java.util.Base64.getDecoder().decode(cursor), java.nio.charset.StandardCharsets.UTF_8);
      String prefix = "offset:";
      if (!raw.startsWith(prefix)) {
        throw new IllegalArgumentException("Invalid cursor: " + cursor);
      }
      return Long.parseLong(raw.substring(prefix.length()));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid cursor: " + cursor, e);
    }
  }

  /// Converts the domain sort list into a Spring Data [Sort].
  private static Sort buildSort(List<MedicineSortInput> sortInputs) {
    if (sortInputs == null || sortInputs.isEmpty()) {
      return Sort.by(Sort.Direction.ASC, "registrationNumber");
    }
    List<Sort.Order> orders = sortInputs.stream()
        .map(s -> {
          Sort.Direction dir = s.direction() == SortDirection.DESC
              ? Sort.Direction.DESC : Sort.Direction.ASC;
          return new Sort.Order(dir, toEntityField(s.field()));
        })
        .toList();
    return Sort.by(orders);
  }

  /// Maps a [MedicineSortField] to the JPA entity field name.
  private static String toEntityField(MedicineSortField field) {
    return switch (field) {
      case REGISTRATION_NUMBER -> "registrationNumber";
      case BRAND_NAME -> "brandName";
      case LABORATORY_HOLDER -> "laboratoryHolder";
      case INITIAL_REGISTRATION_DATE -> "initialRegistrationDate";
    };
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



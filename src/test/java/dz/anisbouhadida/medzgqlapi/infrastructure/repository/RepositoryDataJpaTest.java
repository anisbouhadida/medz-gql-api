package dz.anisbouhadida.medzgqlapi.infrastructure.repository;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineEventType;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.infrastructure.adapter.MedicineAdapter;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEventHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.mapper.MedicineMapper;
import jakarta.persistence.EntityManager;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(RepositoryDataJpaTest.AdapterTestConfig.class)
@ExtendWith(InstancioExtension.class)
@DisplayName("Repository Data JPA integration")
class RepositoryDataJpaTest {

  @Container
  @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:18-alpine");

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private MedicineRepository medicineRepository;

  @Autowired
  private MedicineStatusHistoryRepository medicineStatusHistoryRepository;

  @Autowired
  private MedicineEventHistoryRepository medicineEventHistoryRepository;

  @Autowired
  private NomenclatureEventRepository nomenclatureEventRepository;

  @Autowired
  private WithdrawalEventRepository withdrawalEventRepository;

  @Autowired
  private NonRenewalEventRepository nonRenewalEventRepository;

  @Autowired
  private MedicineAdapter medicineAdapter;

  private MedicineEntity activeImportedMedicine;
  private MedicineEntity activeExactMatchMedicine;
  private MedicineEntity withdrawnMedicine;

  @BeforeEach
  void setUp() {
    nonRenewalEventRepository.deleteAll();
    withdrawalEventRepository.deleteAll();
    nomenclatureEventRepository.deleteAll();
    medicineEventHistoryRepository.deleteAll();
    medicineStatusHistoryRepository.deleteAll();
    medicineRepository.deleteAll();
    entityManager.flush();

    activeImportedMedicine = persistMedicine("REG-001", "CODE-001", "Para%_\\ Relief", "Saidal", MedicineOrigin.IMPORTED);
    activeExactMatchMedicine = persistMedicine("REG-002", "CODE-001", "Clamoxyl", "Biopharm", MedicineOrigin.IMPORTED);
    withdrawnMedicine = persistMedicine("REG-003", "CODE-003", "Para%_\\ Legacy", "SAIDAL", MedicineOrigin.IMPORTED);

    persistStatus(activeImportedMedicine.getMedicineId(), MedicineStatus.ACTIVE, OffsetDateTime.parse("2026-01-01T00:00:00Z"));
    persistStatus(activeExactMatchMedicine.getMedicineId(), MedicineStatus.ACTIVE, OffsetDateTime.parse("2026-01-02T00:00:00Z"));
    persistStatus(withdrawnMedicine.getMedicineId(), MedicineStatus.ACTIVE, OffsetDateTime.parse("2026-01-03T00:00:00Z"));
    persistStatus(withdrawnMedicine.getMedicineId(), MedicineStatus.WITHDRAWN, OffsetDateTime.parse("2026-02-03T00:00:00Z"));

    persistEventHistory(activeImportedMedicine.getMedicineId(), MedicineEventType.UPSERT, OffsetDateTime.parse("2026-03-01T00:00:00Z"), OffsetDateTime.parse("2026-03-10T00:00:00Z"));
    persistEventHistory(activeImportedMedicine.getMedicineId(), MedicineEventType.WITHDRAWAL, OffsetDateTime.parse("2026-03-05T00:00:00Z"), OffsetDateTime.parse("2026-03-20T00:00:00Z"));

    persistNomenclatureEvent(activeImportedMedicine, OffsetDateTime.parse("2026-06-01T00:00:00Z"), "24 MOIS", "Stable");
    persistWithdrawalEvent(withdrawnMedicine, OffsetDateTime.parse("2026-04-01T00:00:00Z"), "Safety concern");
    persistNonRenewalEvent(activeExactMatchMedicine, OffsetDateTime.parse("2026-05-01T00:00:00Z"), "Expired");

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("medicine repository should resolve the expected derived query methods")
  void medicineRepository_shouldResolveDerivedQueryMethods() {
    assertAll(
        () -> assertEquals(Optional.of("REG-001"), medicineRepository.findByRegistrationNumber("REG-001").map(MedicineEntity::getRegistrationNumber)),
        () -> assertEquals(List.of("REG-001", "REG-002"), medicineRepository.findByCode("CODE-001").stream().map(MedicineEntity::getRegistrationNumber).sorted().toList()),
        () -> assertEquals(List.of("REG-001"), medicineRepository.findByIcd("Para%_\\ Relief").stream().map(MedicineEntity::getRegistrationNumber).toList()),
        () -> assertEquals(List.of("REG-002"), medicineRepository.findByBrandName("Clamoxyl").stream().map(MedicineEntity::getRegistrationNumber).toList()),
        () -> assertEquals(List.of("REG-001"), medicineRepository.findByLaboratoryHolder("Saidal").stream().map(MedicineEntity::getRegistrationNumber).toList()));
  }

  @Test
  @DisplayName("status history repository should return only current matches and latest entries")
  void statusHistoryRepository_shouldReturnCurrentMatchesAndLatestEntries() {
    List<Long> currentActiveIds = medicineStatusHistoryRepository.findMedicineIdsByCurrentStatus(MedicineStatus.ACTIVE);
    List<Long> currentWithdrawnIds = medicineStatusHistoryRepository.findMedicineIdsByCurrentStatus(MedicineStatus.WITHDRAWN);
    List<MedicineStatusHistoryEntity> latestEntries = medicineStatusHistoryRepository.findLatestByMedicineIds(
        List.of(activeImportedMedicine.getMedicineId(), withdrawnMedicine.getMedicineId()));

    assertAll(
        () -> assertEquals(List.of(activeImportedMedicine.getMedicineId(), activeExactMatchMedicine.getMedicineId()), currentActiveIds.stream().sorted().toList()),
        () -> assertEquals(List.of(withdrawnMedicine.getMedicineId()), currentWithdrawnIds),
        () -> assertEquals(2, latestEntries.size()),
        () -> assertEquals(MedicineStatus.ACTIVE, latestEntries.stream()
            .filter(entry -> entry.getMedicineId().equals(activeImportedMedicine.getMedicineId()))
            .findFirst()
            .orElseThrow()
            .getStatus()),
        () -> assertEquals(MedicineStatus.WITHDRAWN, latestEntries.stream()
            .filter(entry -> entry.getMedicineId().equals(withdrawnMedicine.getMedicineId()))
            .findFirst()
            .orElseThrow()
            .getStatus()));
  }

  @Test
  @DisplayName("event history repository should support ordering and type filtering")
  void eventHistoryRepository_shouldSupportOrderingAndTypeFiltering() {
    List<MedicineEventHistoryEntity> orderedEvents = medicineEventHistoryRepository
        .findByMedicineIdOrderByUpdatedAtDesc(activeImportedMedicine.getMedicineId());
    List<MedicineEventHistoryEntity> withdrawalEvents = medicineEventHistoryRepository
        .findByEventType(MedicineEventType.WITHDRAWAL);

    assertAll(
        () -> assertEquals(2, orderedEvents.size()),
        () -> assertEquals(MedicineEventType.WITHDRAWAL, orderedEvents.getFirst().getEventType()),
        () -> assertEquals(MedicineEventType.UPSERT, orderedEvents.get(1).getEventType()),
        () -> assertEquals(List.of(activeImportedMedicine.getMedicineId()), withdrawalEvents.stream().map(MedicineEventHistoryEntity::getMedicineId).toList()));
  }

  @Test
  @DisplayName("event repositories should return their mapped-id rows")
  void eventRepositories_shouldReturnMappedIdRows() {
    assertAll(
        () -> assertTrue(nomenclatureEventRepository.findByMedicineId(activeImportedMedicine.getMedicineId()).isPresent()),
        () -> assertTrue(withdrawalEventRepository.findByMedicineId(withdrawnMedicine.getMedicineId()).isPresent()),
        () -> assertTrue(nonRenewalEventRepository.findByMedicineId(activeExactMatchMedicine.getMedicineId()).isPresent()),
        () -> assertEquals(List.of(activeImportedMedicine.getMedicineId()), nomenclatureEventRepository
            .findByMedicineIdIn(List.of(activeImportedMedicine.getMedicineId(), withdrawnMedicine.getMedicineId()))
            .stream()
            .map(NomenclatureEventEntity::getMedicineId)
            .toList()),
        () -> assertEquals(List.of(withdrawnMedicine.getMedicineId()), withdrawalEventRepository
            .findByMedicineIdIn(List.of(activeImportedMedicine.getMedicineId(), withdrawnMedicine.getMedicineId()))
            .stream()
            .map(WithdrawalEventEntity::getMedicineId)
            .toList()),
        () -> assertEquals(List.of(activeExactMatchMedicine.getMedicineId()), nonRenewalEventRepository
            .findByMedicineIdIn(List.of(activeExactMatchMedicine.getMedicineId(), withdrawnMedicine.getMedicineId()))
            .stream()
            .map(NonRenewalEventEntity::getMedicineId)
            .toList()));
  }

  @Test
  @DisplayName("medicine adapter should execute the specification search against PostgreSQL")
  void medicineAdapter_shouldExecuteSpecificationSearchAgainstPostgreSql() {
    MedicineSearchFilter filter = new MedicineSearchFilter(
        "para%_\\",
        MedicineOrigin.IMPORTED,
        MedicineStatus.ACTIVE,
        List.of("saidal"));

    List<Medicine> results = medicineAdapter.search(filter);

    assertEquals(List.of(activeImportedMedicine.getRegistrationNumber()), results.stream().map(Medicine::registrationNumber).toList());
  }

  private MedicineEntity persistMedicine(
      String registrationNumber,
      String code,
      String icd,
      String laboratoryHolder,
      MedicineOrigin origin) {
    MedicineEntity entity = Instancio.of(MedicineEntity.class)
        .set(field(MedicineEntity::getMedicineId), null)
        .set(field(MedicineEntity::getRegistrationNumber), registrationNumber)
        .set(field(MedicineEntity::getCode), code)
        .set(field(MedicineEntity::getIcd), icd)
        .set(field(MedicineEntity::getBrandName), icd)
        .set(field(MedicineEntity::getLaboratoryHolder), laboratoryHolder)
        .set(field(MedicineEntity::getOrigin), origin)
        .create();
    entityManager.persist(entity);
    return entity;
  }

  private void persistStatus(Long medicineId, MedicineStatus status, OffsetDateTime timestamp) {
    MedicineStatusHistoryEntity entity = Instancio.of(MedicineStatusHistoryEntity.class)
        .set(field(MedicineStatusHistoryEntity::getMedicineId), medicineId)
        .set(field(MedicineStatusHistoryEntity::getStatus), status)
        .set(field(MedicineStatusHistoryEntity::getStatusTimestamp), timestamp)
        .create();
    entityManager.persist(entity);
  }

  private void persistEventHistory(Long medicineId, MedicineEventType eventType, OffsetDateTime eventDate, OffsetDateTime updatedAt) {
    MedicineEventHistoryEntity entity = Instancio.of(MedicineEventHistoryEntity.class)
        .set(field(MedicineEventHistoryEntity::getMedicineId), medicineId)
        .set(field(MedicineEventHistoryEntity::getEventType), eventType)
        .set(field(MedicineEventHistoryEntity::getEventDate), eventDate)
        .set(field(MedicineEventHistoryEntity::getUpdatedAt), updatedAt)
        .create();
    entityManager.persist(entity);
  }

  private void persistNomenclatureEvent(MedicineEntity medicine, OffsetDateTime finalRegistrationDate, String stabilityDuration, String observations) {
    NomenclatureEventEntity entity = Instancio.of(NomenclatureEventEntity.class)
        .set(field(NomenclatureEventEntity::getMedicine), medicine)
        .set(field(NomenclatureEventEntity::getMedicineId), medicine.getMedicineId())
        .set(field(NomenclatureEventEntity::getFinalRegistrationDate), finalRegistrationDate)
        .set(field(NomenclatureEventEntity::getStabilityDuration), stabilityDuration)
        .set(field(NomenclatureEventEntity::getObservations), observations)
        .create();
    entityManager.persist(entity);
  }

  private void persistWithdrawalEvent(MedicineEntity medicine, OffsetDateTime withdrawalDate, String withdrawalReason) {
    WithdrawalEventEntity entity = Instancio.of(WithdrawalEventEntity.class)
        .set(field(WithdrawalEventEntity::getMedicine), medicine)
        .set(field(WithdrawalEventEntity::getMedicineId), medicine.getMedicineId())
        .set(field(WithdrawalEventEntity::getWithdrawalDate), withdrawalDate)
        .set(field(WithdrawalEventEntity::getWithdrawalReason), withdrawalReason)
        .create();
    entityManager.persist(entity);
  }

  private void persistNonRenewalEvent(MedicineEntity medicine, OffsetDateTime finalRegistrationDate, String observations) {
    NonRenewalEventEntity entity = Instancio.of(NonRenewalEventEntity.class)
        .set(field(NonRenewalEventEntity::getMedicine), medicine)
        .set(field(NonRenewalEventEntity::getMedicineId), medicine.getMedicineId())
        .set(field(NonRenewalEventEntity::getFinalRegistrationDate), finalRegistrationDate)
        .set(field(NonRenewalEventEntity::getObservations), observations)
        .create();
    entityManager.persist(entity);
  }


  @TestConfiguration(proxyBeanMethods = false)
  static class AdapterTestConfig {

    @Bean
    MedicineMapper medicineMapper() {
      return Mappers.getMapper(MedicineMapper.class);
    }

    @Bean
    MedicineAdapter medicineAdapter(
        MedicineRepository medicineRepository,
        MedicineStatusHistoryRepository medicineStatusHistoryRepository,
        NomenclatureEventRepository nomenclatureEventRepository,
        WithdrawalEventRepository withdrawalEventRepository,
        NonRenewalEventRepository nonRenewalEventRepository,
        MedicineMapper medicineMapper) {
      return new MedicineAdapter(
          medicineRepository,
          medicineStatusHistoryRepository,
          nomenclatureEventRepository,
          withdrawalEventRepository,
          nonRenewalEventRepository,
          medicineMapper);
    }
  }
}




package dz.anisbouhadida.medzgqlapi.infrastructure.adapter;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.NonRenewalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.WithdrawalEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.MedicineStatusHistoryEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NomenclatureEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.NonRenewalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.entity.WithdrawalEventEntity;
import dz.anisbouhadida.medzgqlapi.infrastructure.mapper.MedicineMapper;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.MedicineRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.MedicineStatusHistoryRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.NomenclatureEventRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.NonRenewalEventRepository;
import dz.anisbouhadida.medzgqlapi.infrastructure.repository.WithdrawalEventRepository;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, InstancioExtension.class})
@DisplayName("MedicineAdapter")
class MedicineAdapterTest {

  @Mock
  private MedicineRepository medicineRepository;

  @Mock
  private MedicineStatusHistoryRepository statusHistoryRepository;

  @Mock
  private NomenclatureEventRepository nomenclatureEventRepository;

  @Mock
  private WithdrawalEventRepository withdrawalEventRepository;

  @Mock
  private NonRenewalEventRepository nonRenewalEventRepository;

  @Mock
  private MedicineMapper mapper;

  @InjectMocks
  private MedicineAdapter medicineAdapter;

  @Nested
  @DisplayName("simple repository delegation")
  class SimpleRepositoryDelegation {

    @Test
    @DisplayName("findByRegistrationNumber should map the repository result")
    void findByRegistrationNumber_shouldMapRepositoryResult() {
      MedicineEntity entity = Instancio.create(MedicineEntity.class);
      Medicine expected = Instancio.create(Medicine.class);
      String regNumber = "REG-1";
      when(medicineRepository.findByRegistrationNumber(regNumber)).thenReturn(Optional.of(entity));
      when(mapper.toDomain(entity)).thenReturn(expected);

      Optional<Medicine> actual = medicineAdapter.findByRegistrationNumber(regNumber);

      assertEquals(Optional.of(expected), actual);
      verify(medicineRepository).findByRegistrationNumber(regNumber);
      verify(mapper).toDomain(entity);
    }
  }

  @Nested
  @DisplayName("search")
  class Search {

    @Test
    @DisplayName("search should return an empty list when the current-status lookup yields no medicine IDs")
    void search_shouldReturnEmptyList_whenStatusLookupReturnsNoMedicineIds() {
      MedicineSearchFilter filter = new MedicineSearchFilter(null, null, MedicineStatus.WITHDRAWN, null);
      when(statusHistoryRepository.findMedicineIdsByCurrentStatus(MedicineStatus.WITHDRAWN)).thenReturn(List.of());

      List<Medicine> actual = medicineAdapter.search(filter);

      assertEquals(List.of(), actual);
      verify(statusHistoryRepository).findMedicineIdsByCurrentStatus(MedicineStatus.WITHDRAWN);
      verify(medicineRepository, never()).findAll(anySpecification());
    }

    @Test
    @DisplayName("search should query the repository and map its results when filters are valid")
    void search_shouldQueryRepositoryAndMapResults_whenFiltersAreValid() {
      MedicineSearchFilter filter = new MedicineSearchFilter(
          "para%_\\",
          MedicineOrigin.IMPORTED,
          MedicineStatus.ACTIVE,
          List.of("Saidal", "Biopharm"));
      MedicineEntity entity = Instancio.create(MedicineEntity.class);
      Medicine expected = Instancio.create(Medicine.class);
      when(statusHistoryRepository.findMedicineIdsByCurrentStatus(MedicineStatus.ACTIVE)).thenReturn(List.of(2L, 3L));
      when(medicineRepository.findAll(anySpecification())).thenReturn(List.of(entity));
      when(mapper.toDomain(entity)).thenReturn(expected);

      List<Medicine> actual = medicineAdapter.search(filter);

      assertEquals(List.of(expected), actual);
      verify(statusHistoryRepository).findMedicineIdsByCurrentStatus(MedicineStatus.ACTIVE);
      verify(medicineRepository).findAll(anySpecification());
      verify(mapper).toDomain(entity);
    }
  }

  @Nested
  @DisplayName("status batching")
  class StatusBatching {

    @Test
    @DisplayName("findLatestStatusByMedicineIds should short-circuit empty input")
    void findLatestStatusByMedicineIds_shouldShortCircuitEmptyInput() {
      Map<Long, MedicineStatus> actual = medicineAdapter.findLatestStatusByMedicineIds(List.of());

      assertEquals(Map.of(), actual);
      verify(statusHistoryRepository, never()).findLatestByMedicineIds(any());
    }

    @Test
    @DisplayName("findLatestStatusByMedicineIds should map latest status entities by medicine ID")
    void findLatestStatusByMedicineIds_shouldMapLatestStatusEntitiesByMedicineId() {
      MedicineStatusHistoryEntity active = Instancio.of(MedicineStatusHistoryEntity.class)
          .set(field(MedicineStatusHistoryEntity::getMedicineId), 1L)
          .set(field(MedicineStatusHistoryEntity::getStatus), MedicineStatus.ACTIVE)
          .create();
      MedicineStatusHistoryEntity withdrawn = Instancio.of(MedicineStatusHistoryEntity.class)
          .set(field(MedicineStatusHistoryEntity::getMedicineId), 2L)
          .set(field(MedicineStatusHistoryEntity::getStatus), MedicineStatus.WITHDRAWN)
          .create();
      when(statusHistoryRepository.findLatestByMedicineIds(List.of(1L, 2L))).thenReturn(List.of(active, withdrawn));

      Map<Long, MedicineStatus> actual = medicineAdapter.findLatestStatusByMedicineIds(List.of(1L, 2L));

      assertEquals(Map.of(1L, MedicineStatus.ACTIVE, 2L, MedicineStatus.WITHDRAWN), actual);
      verify(statusHistoryRepository).findLatestByMedicineIds(List.of(1L, 2L));
    }
  }

  @Nested
  @DisplayName("event batching")
  class EventBatching {

    @Test
    @DisplayName("findEventsByMedicineIds should short-circuit empty input")
    void findEventsByMedicineIds_shouldShortCircuitEmptyInput() {
      Map<Long, List<MedicineEvent>> actual = medicineAdapter.findEventsByMedicineIds(List.of());

      assertEquals(Map.of(), actual);
      verify(nomenclatureEventRepository, never()).findByMedicineIdIn(any());
      verify(withdrawalEventRepository, never()).findByMedicineIdIn(any());
      verify(nonRenewalEventRepository, never()).findByMedicineIdIn(any());
    }

    @Test
    @DisplayName("findEventsByMedicineIds should aggregate the three event repositories in order")
    void findEventsByMedicineIds_shouldAggregateRepositoriesInOrder() {
      NomenclatureEventEntity nomenclatureEntity = Instancio.of(NomenclatureEventEntity.class)
          .set(field(NomenclatureEventEntity::getMedicineId), 1L)
          .create();
      WithdrawalEventEntity withdrawalEntity = Instancio.of(WithdrawalEventEntity.class)
          .set(field(WithdrawalEventEntity::getMedicineId), 1L)
          .create();
      NonRenewalEventEntity nonRenewalEntity = Instancio.of(NonRenewalEventEntity.class)
          .set(field(NonRenewalEventEntity::getMedicineId), 1L)
          .create();
      NomenclatureEvent nomenclatureEvent = Instancio.of(NomenclatureEvent.class)
          .set(field(NomenclatureEvent::medicineId), 1L)
          .create();
      WithdrawalEvent withdrawalEvent = Instancio.of(WithdrawalEvent.class)
          .set(field(WithdrawalEvent::medicineId), 1L)
          .create();
      NonRenewalEvent nonRenewalEvent = Instancio.of(NonRenewalEvent.class)
          .set(field(NonRenewalEvent::medicineId), 1L)
          .create();
      when(nomenclatureEventRepository.findByMedicineIdIn(List.of(1L, 2L))).thenReturn(List.of(nomenclatureEntity));
      when(withdrawalEventRepository.findByMedicineIdIn(List.of(1L, 2L))).thenReturn(List.of(withdrawalEntity));
      when(nonRenewalEventRepository.findByMedicineIdIn(List.of(1L, 2L))).thenReturn(List.of(nonRenewalEntity));
      when(mapper.toDomain(nomenclatureEntity)).thenReturn(nomenclatureEvent);
      when(mapper.toDomain(withdrawalEntity)).thenReturn(withdrawalEvent);
      when(mapper.toDomain(nonRenewalEntity)).thenReturn(nonRenewalEvent);

      Map<Long, List<MedicineEvent>> actual = medicineAdapter.findEventsByMedicineIds(List.of(1L, 2L));

      assertAll(
          () -> assertEquals(List.of(nomenclatureEvent, withdrawalEvent, nonRenewalEvent), actual.get(1L)),
          () -> assertEquals(List.of(), actual.get(2L)));
      verify(nomenclatureEventRepository).findByMedicineIdIn(List.of(1L, 2L));
      verify(withdrawalEventRepository).findByMedicineIdIn(List.of(1L, 2L));
      verify(nonRenewalEventRepository).findByMedicineIdIn(List.of(1L, 2L));
    }
  }

  @SuppressWarnings("unchecked")
  private static Specification<MedicineEntity> anySpecification() {
    return any(Specification.class);
  }
}


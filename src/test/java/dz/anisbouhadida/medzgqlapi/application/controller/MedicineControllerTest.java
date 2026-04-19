package dz.anisbouhadida.medzgqlapi.application.controller;

import dz.anisbouhadida.medzgqlapi.domain.api.MedicineApi;
import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, InstancioExtension.class})
@DisplayName("MedicineController")
class MedicineControllerTest {

  @Mock
  private MedicineApi medicineApi;

  @InjectMocks
  private MedicineController medicineController;

  @Nested
  @DisplayName("query mappings")
  class QueryMappings {

    @Test
    @DisplayName("medicineByRegistrationNumber should return null when no medicine is found")
    void medicineByRegistrationNumber_shouldReturnNull_whenMedicineIsMissing() {
      when(medicineApi.findByRegistrationNumber("REG-404")).thenReturn(Optional.empty());

      Medicine actual = medicineController.medicineByRegistrationNumber("REG-404");

      assertNull(actual);
      verify(medicineApi).findByRegistrationNumber("REG-404");
    }

    @Test
    @DisplayName("medicineSearch should delegate the filter to the API")
    void medicineSearch_shouldDelegateFilterToApi() {
      MedicineSearchFilter filter = new MedicineSearchFilter("para", MedicineOrigin.IMPORTED, MedicineStatus.ACTIVE, List.of("Saidal"));
      List<Medicine> expected = List.of(Instancio.create(Medicine.class));
      when(medicineApi.search(filter)).thenReturn(expected);

      List<Medicine> actual = medicineController.medicinesSearch(filter);

      assertEquals(expected, actual);
      verify(medicineApi).search(filter);
    }
  }

  @Nested
  @DisplayName("batch mappings")
  class BatchMappings {

    @Test
    @DisplayName("status should preserve input ordering while mapping latest statuses")
    void status_shouldPreserveInputOrdering_whenStatusesAreResolvedInBatch() {
      Medicine first = Instancio.of(Medicine.class).set(field(Medicine::id), 1L).create();
      Medicine second = Instancio.of(Medicine.class).set(field(Medicine::id), 2L).create();
      when(medicineApi.findLatestStatusByMedicineIds(List.of(1L, 2L)))
          .thenReturn(Map.of(2L, MedicineStatus.WITHDRAWN, 1L, MedicineStatus.ACTIVE));

      List<MedicineStatus> actual = medicineController.status(List.of(first, second));

      assertEquals(List.of(MedicineStatus.ACTIVE, MedicineStatus.WITHDRAWN), actual);
      verify(medicineApi).findLatestStatusByMedicineIds(List.of(1L, 2L));
    }

    @Test
    @DisplayName("event should return empty lists for medicines without events")
    void event_shouldReturnEmptyLists_whenSomeMedicinesHaveNoEvents() {
      Medicine first = Instancio.of(Medicine.class).set(field(Medicine::id), 1L).create();
      Medicine second = Instancio.of(Medicine.class).set(field(Medicine::id), 2L).create();
      NomenclatureEvent event = Instancio.of(NomenclatureEvent.class)
          .set(field(NomenclatureEvent::medicineId), 1L)
          .create();
      List<MedicineEvent> firstEvents = List.of(event);
      when(medicineApi.findEventsByMedicineIds(List.of(1L, 2L)))
          .thenReturn(Map.of(1L, firstEvents));

      List<List<MedicineEvent>> actual = medicineController.event(List.of(first, second));

      assertAll(
          () -> assertEquals(firstEvents, actual.getFirst()),
          () -> assertEquals(List.of(), actual.get(1)));
      verify(medicineApi).findEventsByMedicineIds(List.of(1L, 2L));
    }
  }
}

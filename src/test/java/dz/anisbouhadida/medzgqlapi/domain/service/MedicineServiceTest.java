package dz.anisbouhadida.medzgqlapi.domain.service;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePage;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePageRequest;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.NomenclatureEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.spi.MedicineSpi;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, InstancioExtension.class})
@DisplayName("MedicineService")
class MedicineServiceTest {

  @Mock
  private MedicineSpi medicineSpi;

  @InjectMocks
  private MedicineService medicineService;

  @Test
  @DisplayName("findByRegistrationNumber should return SPI result when a medicine exists")
  void findByRegistrationNumber_shouldReturnSpiResult_whenMedicineExists() {
    Medicine medicine = Instancio.create(Medicine.class);
    String regNumber = medicine.registrationNumber();
    Optional<Medicine> expected = Optional.of(medicine);
    when(medicineSpi.findByRegistrationNumber(regNumber)).thenReturn(expected);

    Optional<Medicine> actual = medicineService.findByRegistrationNumber(regNumber);

    assertSame(expected, actual);
    verify(medicineSpi).findByRegistrationNumber(regNumber);
  }

  @Test
  @DisplayName("findByCode should delegate to the SPI")
  void findByCode_shouldDelegateToSpi() {
    List<Medicine> expected = List.of(Instancio.create(Medicine.class), Instancio.create(Medicine.class));
    String code = "CODE-1";
    when(medicineSpi.findByCode(code)).thenReturn(expected);

    List<Medicine> actual = medicineService.findByCode(code);

    assertSame(expected, actual);
    verify(medicineSpi).findByCode(code);
  }

  @Test
  @DisplayName("search should delegate the filter and page request to the SPI")
  void search_shouldDelegateFilterAndPageRequestToSpi() {
    MedicineSearchFilter filter = new MedicineSearchFilter("doliprane", MedicineOrigin.IMPORTED, MedicineStatus.ACTIVE, List.of("Saidal"));
    MedicinePageRequest pageRequest = new MedicinePageRequest(10, null, null, null, null);
    MedicinePage expected = new MedicinePage(List.of(Instancio.create(Medicine.class)), 1L, false, false, 0L);
    when(medicineSpi.search(filter, pageRequest)).thenReturn(expected);

    MedicinePage actual = medicineService.search(filter, pageRequest);

    assertSame(expected, actual);
    verify(medicineSpi).search(filter, pageRequest);
  }

  @Test
  @DisplayName("findLatestStatusByMedicineIds should return statuses from the SPI")
  void findLatestStatusByMedicineIds_shouldReturnStatusesFromSpi() {
    List<Long> ids = List.of(10L, 11L);
    Map<Long, MedicineStatus> expected = Map.of(10L, MedicineStatus.ACTIVE, 11L, MedicineStatus.WITHDRAWN);
    when(medicineSpi.findLatestStatusByMedicineIds(ids)).thenReturn(expected);

    Map<Long, MedicineStatus> actual = medicineService.findLatestStatusByMedicineIds(ids);

    assertEquals(expected, actual);
    verify(medicineSpi).findLatestStatusByMedicineIds(ids);
  }

  @Test
  @DisplayName("findEventsByMedicineIds should return events from the SPI")
  void findEventsByMedicineIds_shouldReturnEventsFromSpi() {
    List<Long> ids = List.of(10L);
    NomenclatureEvent event = Instancio.of(NomenclatureEvent.class)
        .set(field(NomenclatureEvent::medicineId), 10L)
        .create();
    Map<Long, List<MedicineEvent>> expected = Map.of(10L, List.of(event));
    when(medicineSpi.findEventsByMedicineIds(ids)).thenReturn(expected);

    Map<Long, List<MedicineEvent>> actual = medicineService.findEventsByMedicineIds(ids);

    assertEquals(expected, actual);
    verify(medicineSpi).findEventsByMedicineIds(ids);
  }
}

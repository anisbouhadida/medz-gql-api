package dz.anisbouhadida.medzgqlapi.domain.service;

import dz.anisbouhadida.medzgqlapi.domain.api.MedicineApi;
import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePage;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePageRequest;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.spi.MedicineSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Domain service implementing the medicine query use-cases.
///
/// Delegates all persistence operations to the [MedicineSpi] outbound port.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Service
@RequiredArgsConstructor
public class MedicineService implements MedicineApi {

  private final MedicineSpi medicineSpi;

  @Override
  public Optional<Medicine> findByRegistrationNumber(String registrationNumber) {
    return medicineSpi.findByRegistrationNumber(registrationNumber);
  }

  @Override
  public List<Medicine> findByCode(String code) {
    return medicineSpi.findByCode(code);
  }

  @Override
  public List<Medicine> findByIcd(String icd) {
    return medicineSpi.findByIcd(icd);
  }

  @Override
  public List<Medicine> findByBrandName(String brandName) {
    return medicineSpi.findByBrandName(brandName);
  }

  @Override
  public List<Medicine> findByLaboratoryHolder(String laboratoryHolder) {
    return medicineSpi.findByLaboratoryHolder(laboratoryHolder);
  }

  @Override
  public MedicinePage search(MedicineSearchFilter filter, MedicinePageRequest pageRequest) {
    return medicineSpi.search(filter, pageRequest);
  }

  @Override
  public Map<Long, MedicineStatus> findLatestStatusByMedicineIds(List<Long> medicineIds) {
    return medicineSpi.findLatestStatusByMedicineIds(medicineIds);
  }

  @Override
  public Map<Long, List<MedicineEvent>> findEventsByMedicineIds(List<Long> medicineIds) {
    return medicineSpi.findEventsByMedicineIds(medicineIds);
  }
}


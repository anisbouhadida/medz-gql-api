package dz.anisbouhadida.medzgqlapi.domain.service;

import dz.anisbouhadida.medzgqlapi.domain.api.MedicineApi;
import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineType;
import dz.anisbouhadida.medzgqlapi.domain.spi.MedicineSpi;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
  public Optional<Medicine> findByCode(String code) {
    return medicineSpi.findByCode(code);
  }

  @Override
  public List<Medicine> findAll(MedicineType type, MedicineOrigin origin, MedicineStatus status) {
    return medicineSpi.findAll(type, origin, status);
  }

  @Override
  public List<MedicineEvent> findEventsByRegistrationNumber(String registrationNumber) {
    return medicineSpi.findEventsByRegistrationNumber(registrationNumber);
  }
}


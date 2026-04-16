package dz.anisbouhadida.medzgqlapi.domain.spi;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineType;
import java.util.List;
import java.util.Optional;

/// Outbound port for medicine query operations.
///
/// Implemented by infrastructure adapters to provide persistence access
/// for reading medicine and event data.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public interface MedicineSpi {

  /// Finds a medicine by its official registration number.
  ///
  /// @param registrationNumber the registration number to look up
  /// @return an [Optional] containing the medicine if found
  Optional<Medicine> findByRegistrationNumber(String registrationNumber);

  /// Finds a medicine by its unique code.
  ///
  /// @param code the unique code to look up
  /// @return an [Optional] containing the medicine if found
  Optional<Medicine> findByCode(String code);

  /// Lists medicines with optional filtering by type, origin, and current status.
  ///
  /// @param type   the medicine type filter, or `null` to skip
  /// @param origin the medicine origin filter, or `null` to skip
  /// @param status the current status filter, or `null` to skip
  /// @return a list of matching medicines (never `null`)
  List<Medicine> findAll(MedicineType type, MedicineOrigin origin, MedicineStatus status);

  /// Retrieves all regulatory events associated with medicines having
  /// the given registration number.
  ///
  /// @param registrationNumber the registration number to look up events for
  /// @return a list of medicine events (never `null`)
  List<MedicineEvent> findEventsByRegistrationNumber(String registrationNumber);
}


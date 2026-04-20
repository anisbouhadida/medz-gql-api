package dz.anisbouhadida.medzgqlapi.domain.spi;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePage;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicinePageRequest;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineSearchFilter;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;

import java.util.List;
import java.util.Map;
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
  /// @return a list of medicines matching the code
  List<Medicine> findByCode(String code);

  /// Finds medicines by ICD.
  List<Medicine> findByIcd(String icd);

  /// Finds medicines by brand name.
  List<Medicine> findByBrandName(String brandName);

  /// Finds medicines by laboratory holder.
  List<Medicine> findByLaboratoryHolder(String laboratoryHolder);

  /// Searches medicines using the given filter and pagination request.
  ///
  /// @param filter      the search filter
  /// @param pageRequest pagination and sort parameters
  /// @return a paginated result of matching medicines
  MedicinePage search(MedicineSearchFilter filter, MedicinePageRequest pageRequest);

  /// Finds the latest status for each of the given medicine IDs.
  ///
  /// @param medicineIds the medicine IDs to look up
  /// @return a map from medicine ID to its current status
  Map<Long, MedicineStatus> findLatestStatusByMedicineIds(List<Long> medicineIds);

  /// Finds all regulatory events for each of the given medicine IDs.
  ///
  /// @param medicineIds the medicine IDs to look up
  /// @return a map from medicine ID to its list of events
  Map<Long, List<MedicineEvent>> findEventsByMedicineIds(List<Long> medicineIds);
}


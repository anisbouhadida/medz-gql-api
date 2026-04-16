package dz.anisbouhadida.medzgqlapi.application.controller;

import dz.anisbouhadida.medzgqlapi.domain.api.MedicineApi;
import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;
import dz.anisbouhadida.medzgqlapi.domain.model.MedicineEvent;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/// GraphQL controller that maps the `Query` type operations defined in
/// `schema.graphqls` to the [MedicineApi] domain port.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Controller
@RequiredArgsConstructor
public class MedicineController {

  private final MedicineApi medicineApi;

  /// Retrieve a medicine by its official registration number.
  @QueryMapping
  public Medicine medicineByRegistrationNumber(@Argument String registrationNumber) {
    return medicineApi.findByRegistrationNumber(registrationNumber).orElse(null);
  }

  /// Retrieve a medicine by its unique code.
  @QueryMapping
  public Medicine medicineByCode(@Argument String code) {
    return medicineApi.findByCode(code).orElse(null);
  }

  /// List all medicines with optional filtering by type, origin and status.
  @QueryMapping
  public List<Medicine> medicines(
      @Argument MedicineType type,
      @Argument MedicineOrigin origin,
      @Argument MedicineStatus status) {
    return medicineApi.findAll(type, origin, status);
  }

  /// Retrieve all regulatory events for medicines matching a registration number.
  @QueryMapping
  public List<MedicineEvent> medicineEvents(@Argument String registrationNumber) {
    return medicineApi.findEventsByRegistrationNumber(registrationNumber);
  }
}


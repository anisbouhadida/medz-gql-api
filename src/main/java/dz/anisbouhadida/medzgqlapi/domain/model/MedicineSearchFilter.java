package dz.anisbouhadida.medzgqlapi.domain.model;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineOrigin;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/// Validated input filter for the medicine search endpoint.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record MedicineSearchFilter(

    @Size(max = 255, message = "searchText must not exceed 255 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]+$", message = "searchText contains invalid characters")
    String searchText,

    MedicineOrigin origin,

    MedicineStatus status,

    @Size(max = 50, message = "laboratoryHolders must not exceed 50 entries")
    List<@NotBlank(message = "Each laboratory holder must not be blank")
         @Size(max = 255, message = "Each laboratory holder must not exceed 255 characters") String> laboratoryHolders) {

  @AssertTrue(message = "At least one filter criterion must be provided")
  boolean isAtLeastOneFilterProvided() {
    return searchText != null
        || origin != null
        || status != null
        || (laboratoryHolders != null && !laboratoryHolders.isEmpty());
  }
}


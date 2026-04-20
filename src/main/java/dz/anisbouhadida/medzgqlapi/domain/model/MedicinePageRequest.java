package dz.anisbouhadida.medzgqlapi.domain.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/// Pagination and sort request parameters for the medicine search endpoint.
///
/// Supports Relay-spec cursor-based pagination (forward with first/after and
/// backward with last/before). Cursors are opaque base64-encoded offsets.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record MedicinePageRequest(

    /// Maximum number of items to return in the forward direction (default 20, max 100).
    @Min(value = 1, message = "first must be at least 1")
    @Max(value = 100, message = "first must not exceed 100")
    Integer first,

    /// Cursor after which items are returned (forward pagination).
    String after,

    /// Maximum number of items to return in the backward direction (max 100).
    @Min(value = 1, message = "last must be at least 1")
    @Max(value = 100, message = "last must not exceed 100")
    Integer last,

    /// Cursor before which items are returned (backward pagination).
    String before,

    /// Sort criteria applied to the result set. Defaults to REGISTRATION_NUMBER ASC.
    List<MedicineSortInput> sort) {

  /// Default page size when neither first nor last is specified.
  public static final int DEFAULT_PAGE_SIZE = 20;

  @AssertTrue(message = "Use either first/after (forward) or last/before (backward), not both")
  boolean isValidPaginationDirection() {
    boolean forwardPresent = first != null || after != null;
    boolean backwardPresent = last != null || before != null;
    return !(forwardPresent && backwardPresent);
  }
}


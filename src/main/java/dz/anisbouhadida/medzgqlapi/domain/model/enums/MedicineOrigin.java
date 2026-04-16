package dz.anisbouhadida.medzgqlapi.domain.model.enums;

/// Describes the manufacturing origin of a [dz.anisbouhadida.medzgqlapi.domain.model.Medicine].
///
/// Sourced from the `status` column of the CSV files, where `F` maps to
/// [#MANUFACTURED] and `I` (or `i`) maps to [#IMPORTED].
///
/// @author Anis Bouhadida
/// @since 0.0.1
public enum MedicineOrigin {
  /// The medicine is manufactured locally.
  MANUFACTURED,
  /// The medicine is imported from abroad.
  IMPORTED
}

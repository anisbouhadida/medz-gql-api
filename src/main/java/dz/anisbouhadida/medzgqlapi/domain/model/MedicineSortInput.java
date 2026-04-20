package dz.anisbouhadida.medzgqlapi.domain.model;

import dz.anisbouhadida.medzgqlapi.domain.model.enums.MedicineSortField;
import dz.anisbouhadida.medzgqlapi.domain.model.enums.SortDirection;

/// A single sort criterion for medicine search results.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record MedicineSortInput(MedicineSortField field, SortDirection direction) {}


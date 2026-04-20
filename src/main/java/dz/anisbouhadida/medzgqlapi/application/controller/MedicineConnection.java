package dz.anisbouhadida.medzgqlapi.application.controller;

import java.util.List;

/// Relay-spec Connection wrapper for paginated medicine results.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record MedicineConnection(
    List<MedicineEdge> edges,
    PageInfo pageInfo,
    long totalCount) {}


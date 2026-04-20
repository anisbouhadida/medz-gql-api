package dz.anisbouhadida.medzgqlapi.domain.model;

import java.util.List;

/// Neutral pagination envelope returned by the domain search port.
///
/// The application layer (controller) maps this into the GraphQL
/// Connection/Edge/PageInfo shape.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record MedicinePage(
    List<Medicine> content,
    long totalElements,
    boolean hasNextPage,
    boolean hasPreviousPage,
    /// Absolute offset of the first item in this page (used to build cursors).
    long startOffset) {}


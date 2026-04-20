package dz.anisbouhadida.medzgqlapi.application.controller;

import dz.anisbouhadida.medzgqlapi.domain.model.Medicine;

/// A single edge in the MedicineConnection – wraps a node with its cursor.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record MedicineEdge(Medicine node, String cursor) {}


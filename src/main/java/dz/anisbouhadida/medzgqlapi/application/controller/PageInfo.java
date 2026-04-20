package dz.anisbouhadida.medzgqlapi.application.controller;

/// GraphQL PageInfo type – conveys pagination state to the client.
///
/// @author Anis Bouhadida
/// @since 0.0.1
public record PageInfo(
    boolean hasNextPage,
    boolean hasPreviousPage,
    String startCursor,
    String endCursor) {}


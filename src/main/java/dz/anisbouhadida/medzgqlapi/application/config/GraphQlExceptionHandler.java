package dz.anisbouhadida.medzgqlapi.application.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/// Translates validation and unexpected exceptions into user-friendly GraphQL errors.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@Slf4j
@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

  @Override
  protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {

    if (ex instanceof ConstraintViolationException cve) {
      String message = cve.getConstraintViolations().stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining("; "));

      return GraphqlErrorBuilder.newError(env)
          .message(message)
          .errorType(ErrorType.BAD_REQUEST)
          .build();
    }

    log.error("Unexpected error during data fetching", ex);

    return GraphqlErrorBuilder.newError(env)
        .message("Internal server error")
        .errorType(ErrorType.INTERNAL_ERROR)
        .build();
  }
}


package dz.anisbouhadida.medzgqlapi.application.config;

import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.ErrorType;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GraphQlExceptionHandler")
class GraphQlExceptionHandlerTest {

  private final GraphQlExceptionHandler handler = new GraphQlExceptionHandler();

  @Test
  @DisplayName("resolveToSingleError should translate constraint violations into a BAD_REQUEST error")
  void resolveToSingleError_shouldTranslateConstraintViolationsIntoBadRequest() {
    ConstraintViolation<?> firstViolation = mockViolation("registrationNumber must not be blank");
    ConstraintViolation<?> secondViolation = mockViolation("code must not exceed 255 characters");
    Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();
    violations.add(firstViolation);
    violations.add(secondViolation);
    DataFetchingEnvironment environment = mockEnvironment();

    GraphQLError error = handler.resolveToSingleError(new ConstraintViolationException(violations), environment);

    assertNotNull(error);
    assertAll(
        () -> assertEquals(
            Set.of("registrationNumber must not be blank", "code must not exceed 255 characters"),
            Set.of(error.getMessage().split("; "))),
        () -> assertEquals(ErrorType.BAD_REQUEST, error.getErrorType()));
  }

  @Test
  @DisplayName("resolveToSingleError should hide unexpected exceptions behind an INTERNAL_ERROR")
  void resolveToSingleError_shouldHideUnexpectedExceptions() {
    DataFetchingEnvironment environment = mockEnvironment();

    GraphQLError error = handler.resolveToSingleError(new IllegalStateException("boom"), environment);

    assertNotNull(error);
    assertAll(
        () -> assertEquals("Internal server error", error.getMessage()),
        () -> assertEquals(ErrorType.INTERNAL_ERROR, error.getErrorType()));
  }

  @SuppressWarnings("unchecked")
  private static ConstraintViolation<?> mockViolation(String message) {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(message);
    return violation;
  }

  private static DataFetchingEnvironment mockEnvironment() {
    DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(environment.getField()).thenReturn(Field.newField().name("medicineByRegistrationNumber").build());
    when(environment.getExecutionStepInfo()).thenReturn(stepInfo);
    when(stepInfo.getPath()).thenReturn(ResultPath.parse("/medicineByRegistrationNumber"));
    return environment;
  }
}





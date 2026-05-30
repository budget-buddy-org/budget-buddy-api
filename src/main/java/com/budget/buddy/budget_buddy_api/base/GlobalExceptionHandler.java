package com.budget.buddy.budget_buddy_api.base;

import com.budget.buddy.budget_buddy_api.base.exception.ValidationException;
import com.budget.buddy.budget_buddy_api.base.validation.FieldErrorFactory;
import com.budget.buddy.budget_buddy_contracts.generated.model.FieldError;
import com.budget.buddy.budget_buddy_contracts.generated.model.Problem;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.net.URI;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Global exception handler for the application. Converts various exceptions into standardized {@link Problem} responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String RESOURCE_NOT_FOUND = "Resource not found";
  private static final String ACCESS_DENIED = "Access denied";
  private static final String AUTHENTICATION_FAILED = "Authentication failed";

  private static ResponseEntity<Problem> problemResponse(HttpStatus status, @Nullable String detail, WebRequest request) {
    return problemResponse(status, status.getReasonPhrase(), detail, request);
  }

  private static ResponseEntity<Problem> problemResponse(HttpStatus status, String title, @Nullable String detail, WebRequest request) {
    return buildProblemResponse(new Problem(), status, title, detail, request);
  }

  private static ResponseEntity<Problem> buildProblemResponse(Problem problem, HttpStatus status, String title, @Nullable String detail, WebRequest request) {
    problem
        .type(URI.create("about:blank"))
        .title(title)
        .status(status.value())
        .instance(URI.create(getRequestUri(request)));

    // detail is optional (RFC 9457); setDetail accepts null, the fluent detail() builder does not
    problem.setDetail(detail);

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    return new ResponseEntity<>(problem, headers, status);
  }

  private static String getRequestUri(WebRequest request) {
    if (request instanceof ServletWebRequest servletWebRequest) {
      return servletWebRequest.getRequest().getRequestURI();
    }

    return "";
  }

  /**
   * Handles validation exceptions when method arguments are not valid.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
    var errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldErrorFactory::from)
        .toList();

    var problem = new Problem().errors(errors);
    return buildProblemResponse(problem, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "One or more fields are invalid", request);
  }

  /**
   * Handles constraint violation exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
    var errors = ex.getConstraintViolations()
        .stream()
        .map(FieldErrorFactory::from)
        .toList();

    var problem = new Problem().errors(errors);
    return buildProblemResponse(problem, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Constraint violations", request);
  }

  /**
   * Handles cases where a requested resource is not found.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Problem> handleNotFound(NoSuchElementException ex, WebRequest request) {
    log.debug("Entity not found: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND, request);
  }

  /**
   * Handles cases where a requested resource is not found.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Problem> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
    log.debug("Resource not found: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND, request);
  }

  /**
   * Handles access denied exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Problem> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    log.warn("Access denied: {}", ex.getMessage());
    return problemResponse(HttpStatus.FORBIDDEN, ACCESS_DENIED, request);
  }

  /**
   * Handles authentication exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Problem> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return problemResponse(HttpStatus.UNAUTHORIZED, AUTHENTICATION_FAILED, request);
  }

  /**
   * Handles data integrity violation exceptions (e.g., unique constraint violations).
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Problem> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
    log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    return problemResponse(HttpStatus.CONFLICT, "A data conflict occurred", request);
  }

  /**
   * Handles cases where the HTTP message is not readable (e.g., malformed JSON, unknown or
   * mistyped fields). Per-field Jackson failures populate {@code errors[]} with a {@link FieldError}
   * so clients see exactly which field is at fault; other failures fall back to a plain summary.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Problem> handleNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
    log.debug("Malformed request: {}", ex.getMessage());

    return switch (ex.getCause()) {
      case UnrecognizedPropertyException upe -> fieldErrorResponse(upe.getPropertyName(), "unknown field", request);
      case MismatchedInputException mie -> fieldErrorResponse(jsonPath(mie), "invalid value", request);
      case null, default -> problemResponse(HttpStatus.BAD_REQUEST, "The request body could not be read", request);
    };
  }

  private static ResponseEntity<Problem> fieldErrorResponse(String field, String message, WebRequest request) {
    var problem = new Problem().errors(List.of(new FieldError().field(field).message(message)));
    return buildProblemResponse(problem, HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "One or more fields are invalid", request);
  }

  private static String jsonPath(MismatchedInputException ex) {
    if (ex.getPath() == null || ex.getPath().isEmpty()) {
      return "null";
    }
    return ex.getPath().stream()
        .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
        .reduce((a, b) -> a + "." + b)
        .orElse("null");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Problem> handleMissingParam(MissingServletRequestParameterException ex, WebRequest request) {
    log.debug("Missing request parameter: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /**
   * Handles domain validation failures caused by client-supplied data. The message is
   * author-controlled (see {@link ValidationException}) and surfaced to the client as-is.
   *
   * @param ex      the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Problem> handleValidation(ValidationException ex, WebRequest request) {
    log.debug("Validation failed: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Problem> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
    log.debug("Illegal argument: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  /**
   * Handles malformed date/time query or path parameters (e.g. {@code month=invalid}).
   * These are user-input errors and must surface as 400, not 500.
   *
   * @param ex      the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<Problem> handleDateTimeParse(DateTimeParseException ex, WebRequest request) {
    log.debug("Date/time parse failed: {}", ex.getMessage());
    return problemResponse(HttpStatus.BAD_REQUEST, "Invalid date or time value: " + ex.getParsedString(), request);
  }

  /**
   * Handles all other unhandled exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleGeneric(Exception ex, WebRequest request) {
    log.error("Unhandled exception: {}", ex.getMessage(), ex);
    return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
  }

  /**
   * Handles unsupported operation exceptions.
   *
   * @param ex the exception
   * @param request the current web request
   * @return a {@link ResponseEntity} containing a {@link Problem} detail
   */
  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<Problem> handleUnsupported(UnsupportedOperationException ex, WebRequest request) {
    log.warn("Unsupported operation: {}", ex.getMessage());
    return problemResponse(HttpStatus.NOT_IMPLEMENTED, "Operation not supported", request);
  }
}

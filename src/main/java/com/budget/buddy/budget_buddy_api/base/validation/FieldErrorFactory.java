package com.budget.buddy.budget_buddy_api.base.validation;

import com.budget.buddy.budget_buddy_contracts.generated.model.FieldError;
import jakarta.validation.ConstraintViolation;
import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class FieldErrorFactory {

  public static FieldError from(ConstraintViolation<?> violation) {
    Objects.requireNonNull(violation);

    var field = violation.getPropertyPath() != null
        ? violation.getPropertyPath().toString()
        : "null";

    return new FieldError()
        .field(field)
        .message(violation.getMessage());
  }

  public static FieldError from(org.springframework.validation.FieldError fieldError) {
    Objects.requireNonNull(fieldError);
    var defaultMessage = fieldError.getDefaultMessage();
    var message = defaultMessage != null ? defaultMessage : "Invalid value";

    return new FieldError()
        .field(fieldError.getField())
        .message(message);
  }

}

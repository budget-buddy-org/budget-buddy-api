package com.budget.buddy.budget_buddy_api.base.validation;

import com.budget.buddy.budget_buddy_contracts.generated.model.FieldError;
import jakarta.validation.ConstraintViolation;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    return new FieldError()
        .field(fieldError.getField())
        .message(Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Invalid value"));
  }

}

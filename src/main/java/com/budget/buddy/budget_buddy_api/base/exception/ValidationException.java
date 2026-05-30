package com.budget.buddy.budget_buddy_api.base.exception;

/**
 * Signals that a domain validation rule was violated by client-supplied data.
 *
 * <p>Distinct from {@link IllegalArgumentException} (which conventionally indicates a
 * programming error): a {@code ValidationException} is an expected outcome of bad input
 * and is mapped to {@code 400 Bad Request}. The message is intended to be surfaced to the
 * client, so it must not leak internal state.
 */
public class ValidationException extends RuntimeException {

  public ValidationException(String message) {
    super(message);
  }

}

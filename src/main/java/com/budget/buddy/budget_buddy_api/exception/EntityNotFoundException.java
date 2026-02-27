package com.budget.buddy.budget_buddy_api.exception;

import java.util.NoSuchElementException;

public class EntityNotFoundException extends NoSuchElementException {

  public EntityNotFoundException() {
    super();
  }

  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public EntityNotFoundException(Throwable cause) {
    super(cause);
  }

}

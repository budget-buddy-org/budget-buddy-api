package com.budget.buddy.budget_buddy_api.security.exception;

import java.util.List;

public class InvalidPasswordException extends IllegalArgumentException {

  public InvalidPasswordException(List<String> messages) {
    super(String.join(", ", messages));
  }
}

package com.budget.buddy.budget_buddy_api.security.exception;

public class UsernameAlreadyTakenException extends IllegalArgumentException {

  public UsernameAlreadyTakenException() {
    super("Username already taken");
  }

  public UsernameAlreadyTakenException(String username) {
    super(String.format("Username [%s] already taken", username));
  }
}

package com.budget.buddy.budget_buddy_api.user;

import java.util.Objects;
import java.util.UUID;

public record UserDto(UUID id, String username, boolean enabled) {

  public UserDto {
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(username, "Username cannot be null");
  }
}

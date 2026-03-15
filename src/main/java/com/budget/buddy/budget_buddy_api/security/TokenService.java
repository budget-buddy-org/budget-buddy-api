package com.budget.buddy.budget_buddy_api.security;

import com.budget.buddy.budget_buddy_api.user.UserDto;

@FunctionalInterface
public interface TokenService<T> {

  /**
   * Creates token for a provided user.
   *
   * @param user user to generate token for
   * @return T generated token
   */
  T createToken(UserDto user);

}

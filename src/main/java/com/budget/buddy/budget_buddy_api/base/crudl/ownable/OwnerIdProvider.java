package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import org.springframework.security.core.AuthenticationException;

import java.util.function.Supplier;

/**
 * Resolves the I of the currently authenticated owner.
 *
 * <p>The production binding reads the user I from the
 * {@link org.springframework.security.core.context.SecurityContextHolder}
 * — see {@code OidcOwnerIdProvider}.
 *
 * @param <I> the owner identifier type
 */
@FunctionalInterface
public interface OwnerIdProvider<I> extends Supplier<I> {

  /**
   * Returns the I of the currently authenticated owner.
   *
   * @return the current owner's I; never {@code null}
   * @throws AuthenticationException if no authenticated principal is present
   */
  @Override
  I get() throws AuthenticationException;

}

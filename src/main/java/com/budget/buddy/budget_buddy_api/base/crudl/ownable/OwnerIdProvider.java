package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import java.util.function.Supplier;
import org.springframework.security.core.AuthenticationException;

/**
 * Resolves the I of the currently authenticated owner.
 *
 * <p>The production binding reads the user I from the
 * {@link org.springframework.security.core.context.SecurityContextHolder}
 * — see {@code OidcOwnerIdProvider}.
 *
 * @param <OWNER_ID> the owner identifier type
 */
@FunctionalInterface
public interface OwnerIdProvider<OWNER_ID> extends Supplier<OWNER_ID> {

  /**
   * Returns the I of the currently authenticated owner.
   *
   * @return the current owner's I; never {@code null}
   * @throws AuthenticationException if no authenticated principal is present
   */
  @Override
  OWNER_ID get() throws AuthenticationException;

}

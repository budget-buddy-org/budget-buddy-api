package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.security.exception.UsernameAlreadyTakenException;
import com.budget.buddy.budget_buddy_api.user.UserService;
import com.budget.buddy.budget_buddy_contracts.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations. Handles user registration.
 * Token issuance and session management are handled by the external OIDC provider.
 * These methods will be fully removed in issue #117.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final RegisterRequestValidator registerRequestValidator;

  /**
   * Register a new user with default role
   *
   * @param request registration request containing username and password
   * @throws UsernameAlreadyTakenException if username is already taken
   */
  @Transactional
  public void register(RegisterRequest request) {
    registerRequestValidator.validate(request);
    userService.create(request);
  }

  /**
   * @deprecated Token issuance moved to external OIDC provider. Will be removed in #117.
   */
  @Deprecated(forRemoval = true)
  @Transactional
  public AuthToken login(String username, String password) {
    throw new UnsupportedOperationException("Token issuance moved to external OIDC provider (see #117)");
  }

  /**
   * @deprecated Token issuance moved to external OIDC provider. Will be removed in #117.
   */
  @Deprecated(forRemoval = true)
  @Transactional
  public AuthToken refresh(String refreshToken) {
    throw new UnsupportedOperationException("Token issuance moved to external OIDC provider (see #117)");
  }

  /**
   * @deprecated Session revocation moved to external OIDC provider. Will be removed in #117.
   */
  @Deprecated(forRemoval = true)
  @Transactional
  public void logout() {
    throw new UnsupportedOperationException("Session revocation moved to external OIDC provider (see #117)");
  }

}

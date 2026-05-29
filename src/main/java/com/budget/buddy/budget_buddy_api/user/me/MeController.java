package com.budget.buddy.budget_buddy_api.user.me;

import com.budget.buddy.budget_buddy_contracts.generated.api.UsersApi;
import com.budget.buddy.budget_buddy_contracts.generated.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implements the user self-management endpoints.
 * <p>
 * Every operation currently throws {@link UnsupportedOperationException}, which the
 * {@code GlobalExceptionHandler} maps to a {@code 501 Not Implemented} Problem response.
 * Replace each stub with a real implementation as the feature is built.
 */
@RestController
public class MeController implements UsersApi {

  @Override
  public ResponseEntity<Me> getCurrentUser() {
    throw notImplemented("getCurrentUser");
  }

  @Override
  public ResponseEntity<Void> deleteCurrentUser() {
    throw notImplemented("deleteCurrentUser");
  }

  @Override
  public ResponseEntity<Void> clearCurrentUserData() {
    throw notImplemented("clearCurrentUserData");
  }

  @Override
  public ResponseEntity<UserPreferences> getCurrentUserPreferences() {
    throw notImplemented("getCurrentUserPreferences");
  }

  @Override
  public ResponseEntity<UserPreferences> updateCurrentUserPreferences(UserPreferencesWrite body) {
    throw notImplemented("updateCurrentUserPreferences");
  }

  @Override
  public ResponseEntity<List<ClientSettings>> listCurrentUserClientSettings() {
    throw notImplemented("listCurrentUserClientSettings");
  }

  @Override
  public ResponseEntity<ClientSettings> getCurrentUserClientSettings(String clientId) {
    throw notImplemented("getCurrentUserClientSettings");
  }

  @Override
  public ResponseEntity<ClientSettings> upsertCurrentUserClientSettings(String clientId, ClientSettingsWrite body) {
    throw notImplemented("upsertCurrentUserClientSettings");
  }

  @Override
  public ResponseEntity<Void> deleteCurrentUserClientSettings(String clientId) {
    throw notImplemented("deleteCurrentUserClientSettings");
  }

  private static UnsupportedOperationException notImplemented(String operationId) {
    return new UnsupportedOperationException(operationId + " is not yet implemented");
  }
}

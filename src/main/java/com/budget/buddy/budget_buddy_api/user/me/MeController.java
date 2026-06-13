package com.budget.buddy.budget_buddy_api.user.me;

import com.budget.buddy.budget_buddy_api.user.me.preferences.UserPreferencesService;
import com.budget.buddy.budget_buddy_contracts.generated.api.UsersApi;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettings;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettingsWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.Me;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferences;
import com.budget.buddy.budget_buddy_contracts.generated.model.UserPreferencesWrite;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements the user self-management endpoints.
 * <p>
 * Operations that are not yet implemented throw {@link UnsupportedOperationException}, which the
 * {@code GlobalExceptionHandler} maps to a {@code 501 Not Implemented} Problem response. Replace
 * each remaining stub with a real implementation as the feature is built.
 */
@RestController
@RequiredArgsConstructor
public class MeController implements UsersApi {

  private final UserDataDeletionService dataDeletionService;
  private final UserPreferencesService preferencesService;

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
    dataDeletionService.deleteUserData();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<UserPreferences> getCurrentUserPreferences() {
    return ResponseEntity.ok(preferencesService.get());
  }

  @Override
  public ResponseEntity<UserPreferences> updateCurrentUserPreferences(UserPreferencesWrite body) {
    return ResponseEntity.ok(preferencesService.update(body));
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

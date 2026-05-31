package com.budget.buddy.budget_buddy_api.user.me.settings;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettings;
import com.budget.buddy.budget_buddy_contracts.generated.model.ClientSettingsWrite;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads, stores, and deletes the authenticated user's per-client settings. All operations are scoped
 * to the current user via {@link OwnerIdProvider}, so one user can never see or mutate another's rows.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserClientSettingsService {

  private final UserClientSettingsRepository repository;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  public ClientSettings get(String clientId) {
    return repository.findByUserIdAndClientId(ownerIdProvider.get(), clientId)
        .map(UserClientSettingsService::toModel)
        .orElseThrow(() -> notFound(clientId));
  }

  public List<ClientSettings> list() {
    return repository.findAllByUserId(ownerIdProvider.get()).stream()
        .map(UserClientSettingsService::toModel)
        .toList();
  }

  @Transactional
  public ClientSettings upsert(String clientId, ClientSettingsWrite write) {
    var saved = repository.upsert(ownerIdProvider.get(), clientId, write.getSettings());
    return toModel(saved);
  }

  @Transactional
  public void delete(String clientId) {
    if (repository.deleteByUserIdAndClientId(ownerIdProvider.get(), clientId) == 0) {
      throw notFound(clientId);
    }
  }

  private static EntityNotFoundException notFound(String clientId) {
    return new EntityNotFoundException("No settings stored for client: " + clientId);
  }

  private static ClientSettings toModel(ClientSettingsRow row) {
    return new ClientSettings()
        .clientId(row.clientId())
        .settings(row.settings());
  }
}

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
 * Reads, stores, and deletes the authenticated user's per-client settings. Every query is scoped to
 * the current user via {@link OwnerIdProvider}, so one user can never see or mutate another's rows.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserClientSettingsService {

  private final UserClientSettingsRepository repository;
  private final ClientSettingsMapper mapper;
  private final OwnerIdProvider<UUID> ownerIdProvider;

  public ClientSettings get(String clientId) {
    return repository.findByOwnerIdAndClientId(ownerIdProvider.get(), clientId)
        .map(mapper::toModel)
        .orElseThrow(() -> notFound(clientId));
  }

  public List<ClientSettings> list() {
    return mapper.toModelList(repository.findByOwnerIdOrderByCreatedAt(ownerIdProvider.get()));
  }

  @Transactional
  public ClientSettings upsert(String clientId, ClientSettingsWrite write) {
    var ownerId = ownerIdProvider.get();
    var entity = repository.findByOwnerIdAndClientId(ownerId, clientId)
        .orElseGet(() -> {
          var fresh = new UserClientSettingsEntity();
          fresh.setOwnerId(ownerId);
          fresh.setClientId(clientId);
          return fresh;
        });
    mapper.updateSettings(write, entity);
    return mapper.toModel(repository.save(entity));
  }

  @Transactional
  public void delete(String clientId) {
    var entity = repository.findByOwnerIdAndClientId(ownerIdProvider.get(), clientId)
        .orElseThrow(() -> notFound(clientId));
    repository.delete(entity);
  }

  private static EntityNotFoundException notFound(String clientId) {
    return new EntityNotFoundException("No settings stored for client: " + clientId);
  }
}

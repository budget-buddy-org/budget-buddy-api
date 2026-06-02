package com.budget.buddy.budget_buddy_api.user.me.settings;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * One per-client settings row, owned by a user and keyed in the API by {@code clientId}
 * (unique per owner). Carries a surrogate {@code id} so Spring Data JDBC can manage it as a normal
 * aggregate — the id is assigned by {@code BaseEntityListener} on insert. {@code settings} is an
 * opaque JSON object persisted as {@code jsonb} via the registered Map converters.
 */
@Table("user_client_settings")
@Getter
@Setter
@NoArgsConstructor
class UserClientSettingsEntity extends AuditableEntity implements OwnableEntity<UUID> {

  @Id
  @Column("id")
  private UUID id;

  @Column("owner_id")
  private UUID ownerId;

  @Column("client_id")
  private String clientId;

  @Column("settings")
  private Map<String, Object> settings;
}

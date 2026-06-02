package com.budget.buddy.budget_buddy_api.user.me.preferences;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A user's global preferences, keyed by {@code user_id} (one row per user). The {@link Version}
 * field drives Spring Data JDBC's insert-vs-update decision, so {@code save} on a freshly built
 * entity inserts and on a loaded one updates — giving PUT its create-on-first-write semantics.
 */
@Table("user_preferences")
@Getter
@Setter
@NoArgsConstructor
class UserPreferencesEntity {

  @Id
  @Column("user_id")
  private UUID userId;

  @Column("language")
  private String language;

  @Column("currency")
  private String currency;

  @Column("timezone")
  private String timezone;

  @Version
  @Column("version")
  private Integer version;

  @CreatedDate
  @Column("created_at")
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column("updated_at")
  private OffsetDateTime updatedAt;
}

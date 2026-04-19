package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * User entity representing a user account in the system. Uses Spring Data JDBC for data access.
 */
@Table("users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AuditableEntity implements BaseEntity<UUID> {

  @Id
  @Column("id")
  private UUID id;

  @Column("username")
  private String username;

  @Column("oidc_subject")
  private String oidcSubject;

  @Column("enabled")
  private boolean enabled;

}

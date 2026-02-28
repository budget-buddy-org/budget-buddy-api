package com.budget.buddy.budget_buddy_api.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Authority entity representing user roles/permissions. Used by Spring Security for role-based access control.
 */
@Setter
@Getter
@Table("authorities")
public class AuthorityEntity {

  private String username;
  private String authority;

  public AuthorityEntity() {
  }

  public AuthorityEntity(String username, String authority) {
    this.username = username;
    this.authority = authority;
  }

}

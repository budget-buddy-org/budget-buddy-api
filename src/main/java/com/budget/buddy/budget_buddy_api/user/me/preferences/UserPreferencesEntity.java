package com.budget.buddy.budget_buddy_api.user.me.preferences;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import java.util.Currency;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_preferences")
@Getter
@Setter
@NoArgsConstructor
public class UserPreferencesEntity extends OwnableEntity<UUID> {

  @Column("language")
  private String language;

  @Column("currency")
  private Currency currency;

  @Column("timezone")
  private String timezone;
}

package com.budget.buddy.budget_buddy_api.user.me.preferences;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budget.buddy.budget_buddy_api.base.mapper.CurrencyMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

@DisplayName("UserPreferencesMapper Unit Tests")
class UserPreferencesMapperTest {

  private final UserPreferencesMapper mapper = new UserPreferencesMapperImpl(new CurrencyMapperImpl());

  @Test
  @DisplayName("toPage should throw UnsupportedOperationException — preferences is a singleton resource")
  void toPage_ShouldThrow() {
    assertThatThrownBy(() -> mapper.toPage(Page.empty()))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("singleton resource");
  }
}

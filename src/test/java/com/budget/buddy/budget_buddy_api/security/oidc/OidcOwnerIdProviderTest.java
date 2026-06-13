package com.budget.buddy.budget_buddy_api.security.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

@DisplayName("OidcOwnerIdProvider Unit Tests")
class OidcOwnerIdProviderTest {

  private final OidcOwnerIdProvider provider = new OidcOwnerIdProvider();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  class Get {

    @Test
    void should_ReturnLocalUserId_When_LocalUserAuthenticationIsPresent() {
      // Given
      var userId = UUID.randomUUID();
      var auth = mock(LocalUserAuthentication.class);
      org.mockito.Mockito.when(auth.getLocalUserId()).thenReturn(userId);
      SecurityContextHolder.getContext().setAuthentication(auth);

      // When
      var result = provider.get();

      // Then
      assertThat(result).isEqualTo(userId);
    }

    @Test
    void should_ThrowInvalidBearerTokenException_When_AuthenticationIsNotLocalUser() {
      // Given
      var auth = new AnonymousAuthenticationToken(
          "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
      SecurityContextHolder.getContext().setAuthentication(auth);

      // When / Then
      assertThatThrownBy(provider::get)
          .isInstanceOf(InvalidBearerTokenException.class)
          .hasMessage("Current user is not authenticated.");
    }

    @Test
    void should_ThrowInvalidBearerTokenException_When_NoAuthenticationInContext() {
      // Given — security context has no authentication (null)
      SecurityContextHolder.clearContext();

      // When / Then
      assertThatThrownBy(provider::get)
          .isInstanceOf(InvalidBearerTokenException.class)
          .hasMessage("Current user is not authenticated.");
    }
  }
}

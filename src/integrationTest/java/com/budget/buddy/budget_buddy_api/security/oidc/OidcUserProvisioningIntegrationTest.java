package com.budget.buddy.budget_buddy_api.security.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_api.user.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@DisplayName("OIDC User Provisioning Integration Tests")
class OidcUserProvisioningIntegrationTest extends BaseMvcIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("should provision a new user on first authenticated request")
  void shouldProvisionNewUserOnFirstRequest() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    // First request — user does not exist yet
    assertThat(userRepository.findByOidcSubject(oidcSubject)).isEmpty();

    var result = mvc.get().uri("/v1/categories")
        .with(jwtForUser(oidcSubject))
        .exchange();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

    // User should now exist in the database
    var user = userRepository.findByOidcSubject(oidcSubject);
    assertThat(user).isPresent();
    assertThat(user.get().getOidcSubject()).isEqualTo(oidcSubject);
    assertThat(user.get().isEnabled()).isTrue();
  }

  @Test
  @DisplayName("should reuse existing user on subsequent requests")
  void shouldReuseExistingUserOnSubsequentRequests() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    // First request — provisions the user
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(oidcSubject))
        .exchange();

    var firstUser = userRepository.findByOidcSubject(oidcSubject);
    assertThat(firstUser).isPresent();
    var userId = firstUser.get().getId();

    // Second request — should reuse the same user
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(oidcSubject))
        .exchange();

    var secondUser = userRepository.findByOidcSubject(oidcSubject);
    assertThat(secondUser).isPresent();
    assertThat(secondUser.get().getId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("should use preferred_username from JWT claims as username")
  void shouldUsePreferredUsernameFromJwtClaims() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    mvc.get().uri("/v1/categories")
        .with(jwt().jwt(j -> j
            .subject(oidcSubject)
            .claim("preferred_username", "johndoe")))
        .exchange();

    var user = userRepository.findByOidcSubject(oidcSubject);
    assertThat(user).isPresent();
    assertThat(user.get().getUsername()).isEqualTo("johndoe");
  }

  @Test
  @DisplayName("should fall back to email when preferred_username is absent")
  void shouldFallBackToEmailWhenPreferredUsernameAbsent() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    mvc.get().uri("/v1/categories")
        .with(jwt().jwt(j -> j
            .subject(oidcSubject)
            .claim("email", "john@example.com")))
        .exchange();

    var user = userRepository.findByOidcSubject(oidcSubject);
    assertThat(user).isPresent();
    assertThat(user.get().getUsername()).isEqualTo("john@example.com");
  }

  @Test
  @DisplayName("should fall back to subject when no username claims are present")
  void shouldFallBackToSubjectWhenNoUsernameClaims() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    mvc.get().uri("/v1/categories")
        .with(jwt().jwt(j -> j.subject(oidcSubject)))
        .exchange();

    var user = userRepository.findByOidcSubject(oidcSubject);
    assertThat(user).isPresent();
    assertThat(user.get().getUsername()).isEqualTo(oidcSubject);
  }

  @Test
  @DisplayName("should reject unauthenticated requests")
  void shouldRejectUnauthenticatedRequests() {
    var result = mvc.get().uri("/v1/categories")
        .exchange();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("should isolate data between different OIDC subjects")
  void shouldIsolateDataBetweenDifferentSubjects() {
    var subject1 = "test-sub-" + UUID.randomUUID();
    var subject2 = "test-sub-" + UUID.randomUUID();

    // Both make requests
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(subject1))
        .exchange();
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(subject2))
        .exchange();

    // Both should have separate user records
    var user1 = userRepository.findByOidcSubject(subject1);
    var user2 = userRepository.findByOidcSubject(subject2);

    assertThat(user1).isPresent();
    assertThat(user2).isPresent();
    assertThat(user1.get().getId()).isNotEqualTo(user2.get().getId());
  }
}

package com.budget.buddy.budget_buddy_api.security.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.exception.InvalidPasswordException;
import com.budget.buddy.budget_buddy_api.security.exception.UsernameAlreadyTakenException;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenService;
import com.budget.buddy.budget_buddy_api.user.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserService userService;
  @Mock
  private RefreshTokenService refreshTokenService;
  @Mock
  private RegisterRequestValidator registerRequestValidator;

  @InjectMocks
  private AuthService authService;

  @Nested
  class RegisterTests {

    @Test
    void should_Register_When_RequestIsValid() {
      // Given
      var request = new RegisterRequest("newuser", "Str0ng!Pass#42");

      // When
      authService.register(request);

      // Then
      verify(registerRequestValidator).validate(request);
      verify(userService).create(request);
    }

    @Test
    void should_ThrowException_When_UsernameAlreadyTaken() {
      // Given
      var request = new RegisterRequest("takenuser", "Str0ng!Pass#42");
      doThrow(new UsernameAlreadyTakenException("takenuser"))
          .when(registerRequestValidator).validate(request);

      // When & Then
      assertThatThrownBy(() -> authService.register(request))
          .as("Should propagate UsernameAlreadyTakenException from the validator")
          .isInstanceOf(UsernameAlreadyTakenException.class)
          .hasMessageContaining("takenuser");

      verifyNoInteractions(userService);
    }

    @Test
    void should_ThrowException_When_PasswordIsInvalid() {
      // Given
      var request = new RegisterRequest("newuser", "weak");
      doThrow(new InvalidPasswordException(List.of("Password must be at least 8 characters")))
          .when(registerRequestValidator).validate(request);

      // When & Then
      assertThatThrownBy(() -> authService.register(request))
          .as("Should propagate InvalidPasswordException from the validator")
          .isInstanceOf(InvalidPasswordException.class)
          .hasMessageContaining("at least 8 characters");

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class LogoutTests {

    @BeforeEach
    void setUp() {
      SecurityContextHolder.clearContext();
    }

    @Test
    void should_Logout_When_UserIsAuthenticated() {
      // Given
      var userId = UUID.randomUUID();
      var jwt = mock(Jwt.class);
      when(jwt.getSubject()).thenReturn(userId.toString());

      var authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn(jwt);

      var securityContext = mock(SecurityContext.class);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      SecurityContextHolder.setContext(securityContext);

      // When
      authService.logout();

      // Then
      verify(refreshTokenService).revokeAll(userId);
    }
  }
}

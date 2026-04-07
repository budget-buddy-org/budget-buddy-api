package com.budget.buddy.budget_buddy_api.security.auth;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.security.exception.InvalidPasswordException;
import com.budget.buddy.budget_buddy_api.security.exception.UsernameAlreadyTakenException;
import com.budget.buddy.budget_buddy_api.user.UserService;
import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.ValidationResult;

@ExtendWith(MockitoExtension.class)
class RegisterRequestValidatorTest {

  @Mock
  private UserService userService;

  @Mock
  private PasswordValidator passwordValidator;

  @InjectMocks
  private RegisterRequestValidator validator;

  @Nested
  class ValidateTests {

    @Test
    void should_Pass_When_RequestIsValid() {
      // Given
      var request = new RegisterRequest("newuser", "Str0ng!Pass#42");
      var passing = passingResult();
      when(passwordValidator.validate(any(PasswordData.class))).thenReturn(passing);
      when(userService.existsByUsername("newuser")).thenReturn(false);

      // When & Then
      assertThatNoException()
          .as("Valid request should not throw any exception")
          .isThrownBy(() -> validator.validate(request));

      verify(passwordValidator).validate(any(PasswordData.class));
      verify(userService).existsByUsername("newuser");
    }

    @Test
    void should_Throw_InvalidPasswordException_When_PasswordInvalid() {
      // Given
      var request = new RegisterRequest("newuser", "weak");
      var failing = failingResult(List.of("Password must be 8 or more characters in length."));
      when(passwordValidator.validate(any(PasswordData.class))).thenReturn(failing);

      // When & Then
      assertThatThrownBy(() -> validator.validate(request))
          .as("Should throw InvalidPasswordException when validator rejects the password")
          .isInstanceOf(InvalidPasswordException.class)
          .hasMessageContaining("8 or more characters");

      verify(userService, never()).existsByUsername(any());
    }

    @Test
    void should_Throw_UsernameAlreadyTakenException_When_UsernameExists() {
      // Given
      var request = new RegisterRequest("takenuser", "Str0ng!Pass#42");
      var passing = passingResult();
      when(passwordValidator.validate(any(PasswordData.class))).thenReturn(passing);
      when(userService.existsByUsername("takenuser")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> validator.validate(request))
          .as("Should throw UsernameAlreadyTakenException when username is already taken")
          .isInstanceOf(UsernameAlreadyTakenException.class)
          .hasMessageContaining("takenuser");
    }

    @Test
    void should_ValidatePasswordBeforeUsername() {
      // Given — password is invalid AND username is taken
      var request = new RegisterRequest("takenuser", "weak");
      var failing = failingResult(List.of("Password must be 8 or more characters in length."));
      when(passwordValidator.validate(any(PasswordData.class))).thenReturn(failing);

      // When & Then — password failure should surface first, username check never reached
      assertThatThrownBy(() -> validator.validate(request))
          .as("Password validation should run before username check")
          .isInstanceOf(InvalidPasswordException.class);

      verify(userService, never()).existsByUsername(any());
    }

    @Test
    void should_IncludeAllViolations_When_MultipleRulesFail() {
      // Given
      var request = new RegisterRequest("newuser", "weak");
      var messages = List.of(
          "Password must be 8 or more characters in length.",
          "Password must contain 1 or more uppercase characters.",
          "Password must contain 1 or more digit characters."
      );
      var failing = failingResult(messages);
      when(passwordValidator.validate(any(PasswordData.class))).thenReturn(failing);

      // When & Then
      assertThatThrownBy(() -> validator.validate(request))
          .as("Exception message should contain all violation messages")
          .isInstanceOf(InvalidPasswordException.class)
          .hasMessageContaining("uppercase")
          .hasMessageContaining("digit");
    }
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private ValidationResult passingResult() {
    var result = Mockito.mock(ValidationResult.class);
    when(result.isValid()).thenReturn(true);
    return result;
  }

  private ValidationResult failingResult(List<String> messages) {
    var result = Mockito.mock(ValidationResult.class);
    when(result.isValid()).thenReturn(false);
    when(result.getMessages()).thenReturn(messages);
    return result;
  }
}

package com.budget.buddy.budget_buddy_api.security.auth;

import com.budget.buddy.budget_buddy_api.security.exception.InvalidPasswordException;
import com.budget.buddy.budget_buddy_api.security.exception.UsernameAlreadyTakenException;
import com.budget.buddy.budget_buddy_api.user.UserService;
import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterRequestValidator {

  private final UserService userService;
  private final PasswordValidator passwordValidator;

  public void validate(RegisterRequest request) {
    log.debug("Validating user registration request");
    validatePassword(request.getPassword());
    validateUsername(request.getUsername());
    log.debug("Validated user registration request");
  }

  private void validatePassword(String password) {
    var result = passwordValidator.validate(new PasswordData(password));
    if (!result.isValid()) {
      log.debug("Invalid password: {}", result.getMessages());
      throw new InvalidPasswordException(result.getMessages());
    }
  }

  private void validateUsername(String username) {
    if (userService.existsByUsername(username)) {
      log.debug("Username {} already exists", username);
      throw new UsernameAlreadyTakenException(username);
    }
  }
}

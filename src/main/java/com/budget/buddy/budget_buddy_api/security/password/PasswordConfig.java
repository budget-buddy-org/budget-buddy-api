package com.budget.buddy.budget_buddy_api.security.password;

import org.passay.DefaultPasswordValidator;
import org.passay.PasswordValidator;
import org.passay.data.EnglishCharacterData;
import org.passay.resolver.PropertiesMessageResolver;
import org.passay.rule.CharacterRule;
import org.passay.rule.LengthRule;
import org.passay.rule.WhitespaceRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the Passay {@link PasswordValidator} bean with complexity rules applied at registration.
 */
@Configuration
public class PasswordConfig {

  @Bean
  PasswordValidator passwordValidator() {
    return new DefaultPasswordValidator(new PropertiesMessageResolver(), List.of(
        new LengthRule(8, 128),
        new CharacterRule(EnglishCharacterData.UpperCase, 1),
        new CharacterRule(EnglishCharacterData.LowerCase, 1),
        new CharacterRule(EnglishCharacterData.Digit, 1),
        new CharacterRule(EnglishCharacterData.Special, 1),
        new WhitespaceRule()
    ));
  }
}

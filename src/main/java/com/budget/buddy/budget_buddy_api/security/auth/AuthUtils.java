package com.budget.buddy.budget_buddy_api.security.auth;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtils {

  private static Optional<Jwt> toJwt(Object principle) {
    if (principle instanceof Jwt jwt) {
      return Optional.of(jwt);
    }
    return Optional.empty();
  }

  public static <T> T requireCurrentUserId(Converter<String, T> converter) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    return Optional.ofNullable(authentication)
        .map(Authentication::getPrincipal)
        .flatMap(AuthUtils::toJwt)
        .map(JwtClaimAccessor::getSubject)
        .map(converter::convert)
        .orElseThrow(() -> new InvalidBearerTokenException("Current user is not authenticated."));
  }

}

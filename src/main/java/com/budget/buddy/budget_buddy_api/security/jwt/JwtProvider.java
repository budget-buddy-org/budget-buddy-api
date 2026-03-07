package com.budget.buddy.budget_buddy_api.security.jwt;

import java.time.Clock;
import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@RequiredArgsConstructor
public class JwtProvider {

  private final Clock clock;
  @Getter
  private final long validitySeconds;
  private final JwtEncoder jwtEncoder;

  public String create(String username) {
    var claims = buildClaims(username);

    return jwtEncoder
        .encode(JwtEncoderParameters.from(claims))
        .getTokenValue();
  }

  private JwtClaimsSet buildClaims(String username) {
    var now = Instant.now(clock);
    var expiresAt = now.plusSeconds(validitySeconds);

    return JwtClaimsSet.builder()
        .subject(username)
        .issuedAt(now)
        .expiresAt(expiresAt)
        .build();
  }

}

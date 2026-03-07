package com.budget.buddy.budget_buddy_api.security.jwt;

import static org.springframework.security.oauth2.jose.jws.JwsAlgorithms.HS256;

import java.time.Clock;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

  private final Clock clock;
  private final JwtProperties jwtProperties;

  @Bean
  JwtProvider accessTokenProvider(JwtEncoder jwtEncoder) {
    return new JwtProvider(clock, jwtProperties.accessTokenValiditySeconds(), jwtEncoder);
  }

  @Bean
  JwtProvider refreshTokenProvider(JwtEncoder jwtEncoder) {
    return new JwtProvider(clock, jwtProperties.refreshTokenValiditySeconds(), jwtEncoder);
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder
        .withSecretKey(buildSecretKey())
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  @Bean
  JwtEncoder jwtEncoder() {
    return NimbusJwtEncoder
        .withSecretKey(buildSecretKey())
        .algorithm(MacAlgorithm.HS256)
        .build();
  }

  private SecretKey buildSecretKey() {
    var secret = jwtProperties.secret().getBytes();
    return new SecretKeySpec(secret, HS256);
  }

}

package com.budget.buddy.budget_buddy_api.security.refresh.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.user.UserDto;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  static final long VALIDITY_SECONDS = 1209600L;
  static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");
  static final OffsetDateTime NOW_OFFSET = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
  static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneId.of("UTC"));

  @Mock
  RefreshTokenProvider tokenProvider;
  @Mock
  RefreshTokenRepository repository;
  @Mock
  RefreshTokenProperties properties;

  RefreshTokenService service;

  @BeforeEach
  void init() {
    service = new RefreshTokenService(FIXED_CLOCK, tokenProvider, repository, properties);
  }

  @Nested
  class CreateToken {

    @Test
    void should_CreateAndPersistToken() {
      var userId = UUID.randomUUID();
      var user = new UserDto(userId, "testuser", true);
      var generatedToken = "generated-token-value";
      var savedEntity = new RefreshTokenEntity();
      savedEntity.setToken(generatedToken);

      when(tokenProvider.get()).thenReturn(generatedToken);
      when(properties.validitySeconds()).thenReturn(VALIDITY_SECONDS);
      when(repository.save(any())).thenReturn(savedEntity);

      var result = service.createToken(user);

      assertThat(result).isEqualTo(generatedToken);
      verify(repository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void should_SetCorrectEntityFields() {
      var userId = UUID.randomUUID();
      var user = new UserDto(userId, "testuser", true);
      var savedEntity = new RefreshTokenEntity();
      savedEntity.setToken("token");

      when(tokenProvider.get()).thenReturn("token");
      when(properties.validitySeconds()).thenReturn(VALIDITY_SECONDS);
      when(repository.save(any())).thenReturn(savedEntity);

      service.createToken(user);

      var captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
      verify(repository).save(captor.capture());

      assertThat(captor.getValue())
          .returns(userId, RefreshTokenEntity::getUserId)
          .returns(NOW_OFFSET, RefreshTokenEntity::getCreatedAt)
          .returns(NOW_OFFSET.plusSeconds(VALIDITY_SECONDS), RefreshTokenEntity::getExpiresAt);
    }
  }

  @Nested
  class Rotate {

    @Test
    void should_ReturnEntity_And_DeleteOldToken_When_ValidToken() {
      var token = "valid-token";
      var entity = new RefreshTokenEntity();
      entity.setToken(token);
      entity.setUserId(UUID.randomUUID());

      when(repository.findValidToken(token, NOW_OFFSET)).thenReturn(Optional.of(entity));

      var result = service.rotate(token);

      assertThat(result)
          .returns(token, RefreshTokenEntity::getToken)
          .returns(entity.getUserId(), RefreshTokenEntity::getUserId);

      verify(repository).delete(entity);
    }

    @Test
    void should_Throw_When_TokenInvalidOrExpired() {
      when(repository.findValidToken(any(), any())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.rotate("invalid-or-expired-token"))
          .isInstanceOf(BadCredentialsException.class)
          .hasMessage("Refresh token is invalid");
    }
  }

  @Nested
  class RevokeAll {

    @Test
    void should_DeleteAllTokensForUser() {
      var userId = UUID.randomUUID();

      service.revokeAll(userId);

      verify(repository).deleteAllByUserId(userId);
    }
  }

  @Nested
  class DeleteExpired {

    @Test
    void should_DeleteExpiredTokens() {
      service.deleteExpired();

      verify(repository).deleteAllExpired(NOW_OFFSET);
    }
  }
}

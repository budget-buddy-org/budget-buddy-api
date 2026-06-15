package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private Supplier<UUID> idGenerator;

  @Mock
  private OwnerIdProvider<UUID> ownerIdProvider;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, idGenerator, ownerIdProvider);
  }

  @Test
  void deleteCurrentUser_Should_DeleteByResolvedOwnerId() {
    var userId = UUID.randomUUID();
    when(ownerIdProvider.get()).thenReturn(userId);

    userService.deleteCurrentUser();

    verify(userRepository).deleteById(userId);
  }

  @Test
  void upsert_Should_DelegateToRepository() {
    // Given
    var subject = "test-sub";
    var issuer = "test-issuer";
    var userId = UUID.randomUUID();
    var existingId = UUID.randomUUID();
    when(idGenerator.get()).thenReturn(userId);
    when(userRepository.upsert(userId, subject, issuer)).thenReturn(existingId);

    // When
    UUID actual = userService.findOrCreateByOidcSubject(subject, issuer);

    // Then
    assertThat(actual).isEqualTo(existingId);
  }

}

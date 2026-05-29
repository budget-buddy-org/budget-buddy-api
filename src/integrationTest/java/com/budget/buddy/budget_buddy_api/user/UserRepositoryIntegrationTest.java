package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  void upsert_Should_InsertAndReturnId_When_NoUserWithSameSubject() {
    // Given
    var newId = UUID.randomUUID();
    var subject = "sub_" + UUID.randomUUID();
    var issuer = "test_issuer";

    // When
    UUID actual = userRepository.upsert(newId, subject, issuer);

    // Then
    assertThat(actual)
        .as("Returned ID should be equal to new ID")
        .isEqualTo(newId);
  }

  @Test
  void upsert_Should_ReturnOldId_When_UserWithSameSubject() {
    // Given
    var oldId = UUID.randomUUID();
    var subject = "sub_" + UUID.randomUUID();
    var issuer = "test_issuer";

    userRepository.upsert(oldId, subject, issuer);

    var newId = UUID.randomUUID();

    // When
    UUID actual = userRepository.upsert(newId, subject, issuer);

    // Then
    assertThat(actual)
        .as("Returned ID should be equal to old ID")
        .isEqualTo(oldId);
  }

  @Test
  @DisplayName("Concurrent first logins all resolve to the same non-null id")
  void upsert_Should_ReturnSameNonNullId_When_ConcurrentFirstLogins() throws Exception {
    // Given a brand-new OIDC identity that several requests try to provision at once.
    // The worker threads call the repository outside the test's transaction, so each upsert
    // commits independently — reproducing the real concurrent first-login race. With the old
    // ON CONFLICT DO NOTHING query the losing threads got a null id back.
    var subject = "sub_" + UUID.randomUUID();
    var issuer = "test_issuer";
    var threadCount = 16;

    var pool = Executors.newFixedThreadPool(threadCount);
    var barrier = new CyclicBarrier(threadCount);
    var tasks = new ArrayList<Callable<UUID>>();
    for (var i = 0; i < threadCount; i++) {
      tasks.add(() -> {
        barrier.await(); // release all threads at once to maximise overlap
        return userRepository.upsert(UUID.randomUUID(), subject, issuer);
      });
    }

    // When
    List<UUID> results = new ArrayList<>();
    try {
      var futures = pool.invokeAll(tasks, 30, TimeUnit.SECONDS);
      for (Future<UUID> future : futures) {
        results.add(future.get());
      }
    } finally {
      pool.shutdownNow();
    }

    // Then every concurrent caller must resolve to the same, non-null user id.
    assertThat(results)
        .as("no concurrent first-login upsert should return a null id")
        .doesNotContainNull();
    assertThat(results)
        .as("all concurrent first-login upserts should resolve to the same user id")
        .containsOnly(results.getFirst());
  }
}

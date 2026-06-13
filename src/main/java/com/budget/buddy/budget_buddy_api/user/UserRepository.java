package com.budget.buddy.budget_buddy_api.user;

import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for User entity operations using Spring Data JDBC.
 */
public interface UserRepository extends Repository<UserEntity, UUID> {

  String UPSERT_QUERY = """
      INSERT INTO users (id, oidc_subject, oidc_issuer, version, created_at, updated_at)
      VALUES (:id, :oidcSubject, :oidcIssuer, 0, NOW(), NOW())
      ON CONFLICT (oidc_subject, oidc_issuer)
      DO UPDATE SET updated_at = NOW()
      RETURNING id
      """;

  /**
   * Atomically inserts a new user if none exists for the given OIDC identity, returning the
   * resolved local user id either way.
   *
   * <p>Uses {@code ON CONFLICT DO UPDATE ... RETURNING id} rather than {@code DO NOTHING}.
   * {@code DO NOTHING} returns no row on conflict, so during a concurrent first login — several
   * requests racing before any has committed — the losing requests would read no row under their
   * pre-commit snapshot and get a {@code null} id back. With {@code DO UPDATE} the conflicting row
   * is always returned, so every concurrent caller resolves to the same non-null id. This matters
   * because the dashboard fires multiple requests in parallel on first load; a null id here
   * propagates as a null {@code ownerId} and fails every ownable query with a 400.
   *
   * <p>On conflict, {@code updated_at} is refreshed to {@code NOW()} so it reflects the user's
   * last login time.
   *
   * @return {@link UUID} id of the user
   */
  @Query(UPSERT_QUERY)
  UUID upsert(
      @Param("id") UUID id,
      @Param("oidcSubject") String oidcSubject,
      @Param("oidcIssuer") String oidcIssuer
  );

}

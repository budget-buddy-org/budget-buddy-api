package com.budget.buddy.budget_buddy_api.user.me.preferences;

import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Stores the authenticated user's global preferences (one row per user, keyed by {@code user_id}).
 * Uses {@link JdbcClient} with an {@code ON CONFLICT} upsert so the "create on first write, replace
 * thereafter" semantics are a single atomic round-trip.
 */
@Repository
public class UserPreferencesRepository {

  private static final String USER_ID = "userId";
  private static final String LANGUAGE = "language";
  private static final String CURRENCY = "currency";
  private static final String TIMEZONE = "timezone";

  private static final RowMapper<UserPreferencesRow> ROW_MAPPER = (rs, rowNum) ->
      new UserPreferencesRow(rs.getString("language"), rs.getString("currency"), rs.getString("timezone"));

  private static final String FIND_SQL = """
      SELECT language, currency, timezone
      FROM user_preferences
      WHERE user_id = :userId
      """;

  private static final String UPSERT_SQL = """
      INSERT INTO user_preferences (user_id, language, currency, timezone)
      VALUES (:userId, :language, :currency, :timezone)
      ON CONFLICT (user_id) DO UPDATE
      SET language   = EXCLUDED.language,
          currency   = EXCLUDED.currency,
          timezone   = EXCLUDED.timezone,
          updated_at = clock_timestamp()
      RETURNING language, currency, timezone
      """;

  private final JdbcClient jdbcClient;

  public UserPreferencesRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public Optional<UserPreferencesRow> findByUserId(UUID userId) {
    return jdbcClient.sql(FIND_SQL)
        .param(USER_ID, userId)
        .query(ROW_MAPPER)
        .optional();
  }

  public UserPreferencesRow upsert(UUID userId, UserPreferencesRow preferences) {
    return jdbcClient.sql(UPSERT_SQL)
        .param(USER_ID, userId)
        .param(LANGUAGE, preferences.language())
        .param(CURRENCY, preferences.currency())
        .param(TIMEZONE, preferences.timezone())
        .query(ROW_MAPPER)
        .single();
  }
}

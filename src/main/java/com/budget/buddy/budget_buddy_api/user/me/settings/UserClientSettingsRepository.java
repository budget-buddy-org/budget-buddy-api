package com.budget.buddy.budget_buddy_api.user.me.settings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Stores per-client settings rows keyed by {@code (user_id, client_id)}. The opaque {@code settings}
 * object is persisted as {@code jsonb}: serialised on write via {@link ObjectMapper} and bound with
 * a {@code ::jsonb} cast, read back from the {@code jsonb} text in the {@link RowMapper}. Upserts use
 * {@code ON CONFLICT} so create-or-replace is a single atomic round-trip.
 */
@Repository
public class UserClientSettingsRepository {

  private static final TypeReference<Map<String, Object>> SETTINGS_TYPE = new TypeReference<>() {
  };

  private static final String USER_ID = "userId";
  private static final String CLIENT_ID = "clientId";
  private static final String SETTINGS = "settings";

  private static final String FIND_SQL = """
      SELECT client_id, settings
      FROM user_client_settings
      WHERE user_id = :userId AND client_id = :clientId
      """;

  private static final String LIST_SQL = """
      SELECT client_id, settings
      FROM user_client_settings
      WHERE user_id = :userId
      ORDER BY created_at
      """;

  private static final String UPSERT_SQL = """
      INSERT INTO user_client_settings (user_id, client_id, settings)
      VALUES (:userId, :clientId, CAST(:settings AS jsonb))
      ON CONFLICT (user_id, client_id) DO UPDATE
      SET settings   = EXCLUDED.settings,
          updated_at = clock_timestamp()
      RETURNING client_id, settings
      """;

  private static final String DELETE_SQL = """
      DELETE FROM user_client_settings
      WHERE user_id = :userId AND client_id = :clientId
      """;

  private final JdbcClient jdbcClient;
  private final ObjectMapper objectMapper;
  private final RowMapper<ClientSettingsRow> rowMapper;

  public UserClientSettingsRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
    this.jdbcClient = jdbcClient;
    this.objectMapper = objectMapper;
    this.rowMapper = (rs, rowNum) ->
        new ClientSettingsRow(
            rs.getString("client_id"),
            objectMapper.readValue(rs.getString("settings"), SETTINGS_TYPE));
  }

  public Optional<ClientSettingsRow> findByUserIdAndClientId(UUID userId, String clientId) {
    return jdbcClient.sql(FIND_SQL)
        .param(USER_ID, userId)
        .param(CLIENT_ID, clientId)
        .query(rowMapper)
        .optional();
  }

  public List<ClientSettingsRow> findAllByUserId(UUID userId) {
    return jdbcClient.sql(LIST_SQL)
        .param(USER_ID, userId)
        .query(rowMapper)
        .list();
  }

  public ClientSettingsRow upsert(UUID userId, String clientId, Map<String, Object> settings) {
    return jdbcClient.sql(UPSERT_SQL)
        .param(USER_ID, userId)
        .param(CLIENT_ID, clientId)
        .param(SETTINGS, objectMapper.writeValueAsString(settings))
        .query(rowMapper)
        .single();
  }

  public int deleteByUserIdAndClientId(UUID userId, String clientId) {
    return jdbcClient.sql(DELETE_SQL)
        .param(USER_ID, userId)
        .param(CLIENT_ID, clientId)
        .update();
  }
}

package com.budget.buddy.budget_buddy_api.base.converters;

import com.budget.buddy.budget_buddy_api.transaction.TransactionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;

import java.sql.JDBCType;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomJdbcConverters {

  public static final List<Converter<?, ?>> CONVERTERS = List.of(
      new CurrencyReadingConverter(),
      new CurrencyWritingConverter(),
      new TimestampToOffsetDateTimeConverter(),
      new TransactionTypeWritingConverter()
  );

  @ReadingConverter
  static class CurrencyReadingConverter implements Converter<String, Currency> {

    @Override
    public Currency convert(String source) {
      return Currency.getInstance(source);
    }
  }

  @WritingConverter
  static class CurrencyWritingConverter implements Converter<Currency, String> {

    @Override
    public String convert(Currency source) {
      return source.getCurrencyCode();
    }
  }

  @ReadingConverter
  static class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(Timestamp source) {
      return source.toInstant().atOffset(ZoneOffset.UTC);
    }
  }

  @WritingConverter
  static class TransactionTypeWritingConverter implements Converter<TransactionType, JdbcValue> {

    @Override
    public JdbcValue convert(TransactionType source) {
      return JdbcValue.of(source, JDBCType.OTHER);
    }
  }

  /**
   * Serialises a {@code Map} property to a Postgres {@code jsonb} column. Constructed with the
   * Spring-managed {@link ObjectMapper} in {@code JdbcConfig} since it needs JSON serialisation.
   */
  @WritingConverter
  @RequiredArgsConstructor
  public static class MapToJsonbWritingConverter implements Converter<Map<String, Object>, JdbcValue> {

    private final ObjectMapper objectMapper;

    @Override
    public JdbcValue convert(Map<String, Object> source) {
      return JdbcValue.of(objectMapper.writeValueAsString(source), JDBCType.OTHER);
    }
  }

  /**
   * Reads a Postgres {@code jsonb} column (returned by the driver as a {@link PGobject}) back into a
   * {@code Map}.
   */
  @ReadingConverter
  @RequiredArgsConstructor
  public static class JsonbToMapReadingConverter implements Converter<PGobject, Map<String, Object>> {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> convert(PGobject source) {
      var json = source.getValue();
      return json == null ? Map.of() : objectMapper.readValue(json, MAP_TYPE);
    }
  }

}

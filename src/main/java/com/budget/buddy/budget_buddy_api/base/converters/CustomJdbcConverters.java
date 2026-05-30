package com.budget.buddy.budget_buddy_api.base.converters;

import com.budget.buddy.budget_buddy_api.transaction.TransactionType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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

}

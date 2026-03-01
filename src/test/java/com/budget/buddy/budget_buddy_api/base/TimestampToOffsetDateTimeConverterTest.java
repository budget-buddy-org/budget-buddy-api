package com.budget.buddy.budget_buddy_api.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TimestampToOffsetDateTimeConverterTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2024-01-01T00:00:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("Australia/Sydney"));

  private final TimestampToOffsetDateTimeConverter converter =
      new TimestampToOffsetDateTimeConverter(FIXED_CLOCK);

  @Test
  void shouldConvertTimestampToOffsetDateTime() {
    // Given
    var timestamp = Timestamp.from(FIXED_INSTANT);

    // When
    var actual = converter.convert(timestamp);

    // Then
    assertThat(actual).isEqualTo("2024-01-01T11:00+11:00");
  }

  @Test
  void shouldReturnNullWhenTimestampIsNull() {
    // When
    var actual = converter.convert(null);

    // Then
    assertThat(actual).isNull();
  }

}

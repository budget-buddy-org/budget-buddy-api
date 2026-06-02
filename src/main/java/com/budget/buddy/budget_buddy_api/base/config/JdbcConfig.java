package com.budget.buddy.budget_buddy_api.base.config;

import com.budget.buddy.budget_buddy_api.base.converters.CustomJdbcConverters;
import com.budget.buddy.budget_buddy_api.base.converters.CustomJdbcConverters.JsonbToMapReadingConverter;
import com.budget.buddy.budget_buddy_api.base.converters.CustomJdbcConverters.MapToJsonbWritingConverter;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableJdbcAuditing
public class JdbcConfig {

  @Bean
  public DateTimeProvider offsetDateTimeProvider(Clock clock) {
    return () -> Optional.of(OffsetDateTime.now(clock));
  }

  @Bean
  public JdbcCustomConversions jdbcCustomConversions(ObjectMapper objectMapper) {
    var converters = new ArrayList<Converter<?, ?>>(CustomJdbcConverters.CONVERTERS);
    converters.add(new MapToJsonbWritingConverter(objectMapper));
    converters.add(new JsonbToMapReadingConverter(objectMapper));
    return new JdbcCustomConversions(converters);
  }

}

package com.budget.buddy.budget_buddy_api.base.config;

import com.budget.buddy.budget_buddy_api.base.TimestampToOffsetDateTimeConverter;
import com.budget.buddy.budget_buddy_api.base.crudl.AuditableEntityListener;
import com.budget.buddy.budget_buddy_api.category.CategoryEntity;
import com.budget.buddy.budget_buddy_api.transaction.TransactionEntity;
import com.budget.buddy.budget_buddy_api.user.UserEntity;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

@Configuration
public class ApplicationConfig {

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  Supplier<UUID> idGenerator() {
    return UUID::randomUUID;
  }

  @Bean
  JdbcCustomConversions customConversions(Clock clock) {
    return new JdbcCustomConversions(List.of(new TimestampToOffsetDateTimeConverter(clock)));
  }

  @Bean
  AuditableEntityListener<CategoryEntity, UUID> categoryEntityListener(
      Supplier<UUID> idGenerator,
      Clock clock
  ) {
    return new AuditableEntityListener<>(idGenerator, clock);
  }

  @Bean
  AuditableEntityListener<TransactionEntity, UUID> transactionEntityListener(
      Supplier<UUID> idGenerator,
      Clock clock
  ) {
    return new AuditableEntityListener<>(idGenerator, clock);
  }

  @Bean
  AuditableEntityListener<UserEntity, UUID> userEntityListener(
      Supplier<UUID> idGenerator,
      Clock clock
  ) {
    return new AuditableEntityListener<>(idGenerator, clock);
  }

}

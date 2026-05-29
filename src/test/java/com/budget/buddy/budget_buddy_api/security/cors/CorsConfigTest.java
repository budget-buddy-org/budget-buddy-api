package com.budget.buddy.budget_buddy_api.security.cors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(CorsConfig.class)
      .withPropertyValues(
          "security.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
          "security.cors.allowed-headers=*",
          "security.cors.allow-credentials=true",
          "security.cors.max-age=3600"
      );

  @Nested
  class BeanCreationTests {

    @Test
    void should_CreateBean_When_OriginsConfigured() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=https://app.example.com")
          .run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);
          });
    }

    @Test
    void should_CreateBean_When_OriginsEmpty() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=")
          .run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);
          });
    }
  }

  @Nested
  class ConfigurationMappingTests {

    @Test
    void should_ApplyAllowedOriginPatterns() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=https://app.example.com,https://*.cdn.example.com")
          .run(context -> {
            var config = corsConfigFor(context);
            assertThat(config.getAllowedOriginPatterns())
                .as("allowed origin patterns should match configured values")
                .containsExactlyInAnyOrder("https://app.example.com", "https://*.cdn.example.com");
          });
    }

    @Test
    void should_ApplyAllowedMethods() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=https://app.example.com")
          .run(context -> {
            var config = corsConfigFor(context);
            assertThat(config.getAllowedMethods())
                .as("allowed methods should match configured values")
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
          });
    }

    @Test
    void should_ApplyAllowCredentials() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=https://app.example.com")
          .run(context -> {
            var config = corsConfigFor(context);
            assertThat(config.getAllowCredentials())
                .as("allow-credentials should be true")
                .isTrue();
          });
    }

    @Test
    void should_ApplyMaxAge() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=https://app.example.com")
          .run(context -> {
            var config = corsConfigFor(context);
            assertThat(config.getMaxAge())
                .as("max-age should match configured value")
                .isEqualTo(3600L);
          });
    }

    @Test
    void should_ApplyConfigurationToAllPaths() {
      contextRunner
          .withPropertyValues("security.cors.allowed-origin-patterns=https://app.example.com")
          .run(context -> {
            var source = context.getBean(CorsConfigurationSource.class);
            for (var path : new String[] {"/", "/v1/categories", "/v1/transactions", "/actuator/health"}) {
              var request = new MockHttpServletRequest("GET", path);
              assertThat(source.getCorsConfiguration(request))
                  .as("CORS config should apply to path: %s", path)
                  .isNotNull();
            }
          });
    }
  }

  private static CorsConfiguration corsConfigFor(ApplicationContext context) {
    var source = context.getBean(CorsConfigurationSource.class);
    return source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));
  }
}

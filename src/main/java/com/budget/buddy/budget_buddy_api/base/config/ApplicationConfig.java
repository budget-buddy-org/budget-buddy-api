package com.budget.buddy.budget_buddy_api.base.config;

import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_api.security.oidc.OidcUserProvisioningFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class ApplicationConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  Supplier<UUID> idGenerator() {
    return UUID::randomUUID;
  }

  /**
   * Provides the local user UUID set by {@link OidcUserProvisioningFilter}.
   * The filter runs after JWT authentication and maps the OIDC subject to a local user,
   * storing the resulting UUID as a request attribute.
   */
  @Bean
  OwnerIdProvider<UUID> ownerIdProvider() {
    return () -> {
      var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs == null) {
        throw new InvalidBearerTokenException("No request context available.");
      }
      HttpServletRequest request = attrs.getRequest();
      var userId = (UUID) request.getAttribute(OidcUserProvisioningFilter.USER_ID_ATTRIBUTE);
      if (userId == null) {
        throw new InvalidBearerTokenException("Current user is not authenticated.");
      }
      return userId;
    };
  }

}

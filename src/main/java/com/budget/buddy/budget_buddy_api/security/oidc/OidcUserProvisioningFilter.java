package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Upgrades the post-authentication {@link JwtAuthenticationToken} to a
 * {@link LocalUserAuthentication} carrying the resolved local user UUID.
 *
 * <p>On the first request from a new OIDC user, a local row is provisioned
 * automatically (JIT provisioning) — subsequent requests are served from the
 * in-process cache in {@link UserService}.
 *
 * <p>If the principal is already a {@link LocalUserAuthentication} (e.g. the
 * upgrade has run earlier in the chain), the filter is a no-op.
 */
@Slf4j
@RequiredArgsConstructor
public class OidcUserProvisioningFilter extends OncePerRequestFilter {

  private final UserService userService;

  private static final String USER_ID_MDC_KEY = "userId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    var context = SecurityContextHolder.getContext();
    var authentication = context.getAuthentication();

    if (authentication instanceof JwtAuthenticationToken jwtAuth
        && !(authentication instanceof LocalUserAuthentication)) {
      Jwt jwt = jwtAuth.getToken();
      var oidcSubject = jwt.getSubject();
      var oidcIssuer = jwt.getIssuer();

      if (oidcSubject == null || oidcIssuer == null) {
        throw new InvalidBearerTokenException("JWT must contain both 'sub' and 'iss' claims");
      }

      log.debug("Upgrading JWT authentication: issuer={}", oidcIssuer);
      var localUserId = userService.findOrCreateByOidcSubject(oidcSubject, oidcIssuer.toString());
      MDC.put(USER_ID_MDC_KEY, localUserId.toString());
      context.setAuthentication(new LocalUserAuthentication(jwt, jwtAuth.getAuthorities(), localUserId));
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(USER_ID_MDC_KEY);
    }
  }
}

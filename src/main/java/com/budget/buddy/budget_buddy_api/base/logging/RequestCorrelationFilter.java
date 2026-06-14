package com.budget.buddy.budget_buddy_api.base.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Propagates or generates a request correlation ID and makes it available in the
 * MDC under {@value #REQUEST_ID_MDC_KEY} for the duration of the request.
 *
 * <p>If the caller supplies an {@code X-Request-ID} header it is reused;
 * otherwise a new UUID is generated. The same value is echoed back in the
 * response header so clients can correlate their own logs.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-ID";
  public static final String REQUEST_ID_MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {
    var inbound = request.getHeader(REQUEST_ID_HEADER);
    var requestId = (inbound != null && !inbound.isBlank()) ? inbound : UUID.randomUUID().toString();

    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);
    log.debug("{} {}", request.getMethod(), request.getRequestURI());

    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(REQUEST_ID_MDC_KEY);
    }
  }
}

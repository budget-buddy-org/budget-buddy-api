package com.budget.buddy.budget_buddy_api.base.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RequestCorrelationFilterTest {

  @Mock
  private FilterChain filterChain;

  private RequestCorrelationFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new RequestCorrelationFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    MDC.clear();
  }

  @Nested
  class WhenNoRequestIdHeader {

    @Test
    void should_GenerateUuid_And_SetResponseHeader() throws ServletException, IOException {
      filter.doFilterInternal(request, response, filterChain);

      assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER))
          .isNotBlank()
          .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  class WhenBlankRequestIdHeader {

    @Test
    void should_GenerateUuid_When_HeaderIsEmpty() throws ServletException, IOException {
      request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "");

      filter.doFilterInternal(request, response, filterChain);

      assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER))
          .isNotBlank();
      verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_GenerateUuid_When_HeaderIsWhitespace() throws ServletException, IOException {
      request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "   ");

      filter.doFilterInternal(request, response, filterChain);

      assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER))
          .isNotBlank();
      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  class WhenValidRequestIdHeader {

    @Test
    void should_PropagateExistingRequestId() throws ServletException, IOException {
      var existingId = "my-request-id-123";
      request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, existingId);

      filter.doFilterInternal(request, response, filterChain);

      assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER))
          .isEqualTo(existingId);
      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  class MdcLifecycle {

    @Test
    void should_PopulateMdc_During_Request() throws ServletException, IOException {
      var capturedRequestId = new String[1];

      filter.doFilterInternal(request, response, (req, res) -> {
        capturedRequestId[0] = MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY);
      });

      assertThat(capturedRequestId[0]).isNotBlank();
    }

    @Test
    void should_ClearMdc_After_Request() throws ServletException, IOException {
      filter.doFilterInternal(request, response, filterChain);

      assertThat(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY)).isNull();
    }
  }
}

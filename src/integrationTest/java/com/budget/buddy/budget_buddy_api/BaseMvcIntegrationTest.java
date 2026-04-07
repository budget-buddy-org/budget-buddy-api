package com.budget.buddy.budget_buddy_api;

import com.budget.buddy.budget_buddy_contracts.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_contracts.generated.model.LoginRequest;
import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
public abstract class BaseMvcIntegrationTest extends BaseIntegrationTest {

  /**
   * Strong password that satisfies all complexity rules;
   * use this in tests that don't care about the password value.
   */
  protected static final String STRONG_TEST_PASSWORD = "Str0ng!Pass#42";

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvcTester mvc;

  protected String json(Object obj) {
    return objectMapper.writeValueAsString(obj);
  }

  protected <T> T parseBody(MvcTestResult result, Class<T> type) throws Exception {
    return objectMapper.readValue(result.getResponse().getContentAsString(), type);
  }

  protected String registerAndLogin(String username) throws Exception {
    register(username, STRONG_TEST_PASSWORD);
    return login(username, STRONG_TEST_PASSWORD).getAccessToken();
  }

  protected void register(String username, String password) {
    var exchange = mvc.post().uri("/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new RegisterRequest().username(username).password(password)))
        .exchange();

    assertThat(exchange).hasStatus2xxSuccessful();
  }

  protected AuthToken login(String username, String password) throws Exception {
    var exchange = mvc.post().uri("/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(new LoginRequest().username(username).password(password)))
        .exchange();

    assertThat(exchange).hasStatus2xxSuccessful();

    return objectMapper.readValue(
        exchange.getResponse().getContentAsString(), AuthToken.class);
  }

}

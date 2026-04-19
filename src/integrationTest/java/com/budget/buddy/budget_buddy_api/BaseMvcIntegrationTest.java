package com.budget.buddy.budget_buddy_api;

import com.budget.buddy.budget_buddy_api.user.UserEntity;
import com.budget.buddy.budget_buddy_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@AutoConfigureMockMvc
public abstract class BaseMvcIntegrationTest extends BaseIntegrationTest {

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvcTester mvc;

  @Autowired
  private UserRepository userRepository;

  protected String json(Object obj) {
    return objectMapper.writeValueAsString(obj);
  }

  protected <T> T parseBody(MvcTestResult result, Class<T> type) throws Exception {
    return objectMapper.readValue(result.getResponse().getContentAsString(), type);
  }

  /**
   * Creates a test user in the database and returns their ID.
   */
  protected String createTestUser() {
    var user = UserEntity.builder()
        .username("testuser-" + UUID.randomUUID())
        .oidcSubject("sub-" + UUID.randomUUID())
        .enabled(true)
        .build();
    var saved = userRepository.save(user);
    return saved.getId().toString();
  }

  /**
   * Returns a JWT request post-processor for the given user ID.
   */
  protected static RequestPostProcessor jwtForUser(String userId) {
    return jwt().jwt(j -> j.subject(userId));
  }

}

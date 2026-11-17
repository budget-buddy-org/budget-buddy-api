package com.budget.buddy.budget_buddy_api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    return new PostgreSQLContainer(DockerImageName.parse("cgr.dev/chainguard/postgres:latest").asCompatibleSubstituteFor("postgres"))
        .withCreateContainerCmdModifier(command -> command.withEntrypoint("/usr/bin/docker-entrypoint.sh"));
  }
}

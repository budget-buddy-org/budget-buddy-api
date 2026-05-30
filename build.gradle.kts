import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
  java
  `jvm-test-suite`
  jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.nullaway)
}

group = "com.budget.buddy"
description = "API for Budget Buddy App"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/budget-buddy-org/budget-buddy-contracts")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
  }
}

dependencies {
    implementation(libs.budget.buddy.contracts)
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-liquibase")
  implementation("com.github.ben-manes.caffeine:caffeine")
    implementation(libs.mapstruct)
    implementation(libs.jspecify)

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)

  compileOnly("org.projectlombok:lombok")

  runtimeOnly("org.postgresql:postgresql")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor(libs.lombok.mapstruct.binding)
    annotationProcessor(libs.mapstruct.processor)
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testCompileOnly("org.projectlombok:lombok")
    testCompileOnly(libs.mapstruct)
    testAnnotationProcessor(libs.mapstruct.processor)
  testAnnotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor(libs.lombok.mapstruct.binding)
}

@Suppress("UnstableApiUsage")
testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()
    }

    register<JvmTestSuite>("integrationTest") {
      useJUnitJupiter()
      dependencies {
        implementation(project())
        implementation("org.springframework.boot:spring-boot-starter-liquibase-test")
        implementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
        implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
        implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
        implementation("org.springframework.boot:spring-boot-starter-security-test")
        implementation("org.springframework.boot:spring-boot-testcontainers")
        implementation("org.testcontainers:testcontainers-junit-jupiter")
        implementation("org.testcontainers:testcontainers-postgresql")
      }

      targets {
        all {
          testTask.configure {
            shouldRunAfter(test)
          }
        }
      }
    }
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
  named("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
  }
  named("integrationTestCompileOnly") {
    extendsFrom(configurations.testCompileOnly.get())
  }
  named("integrationTestAnnotationProcessor") {
    extendsFrom(configurations.testAnnotationProcessor.get())
  }
}

tasks.named<Test>("test") {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.named<Test>("integrationTest") {
  finalizedBy(tasks.jacocoTestReport)
}

@Suppress("UnstableApiUsage")
tasks.named("check") {
  dependsOn(testing.suites.named("test"))
  dependsOn(testing.suites.named("integrationTest"))
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required = true
  }
  executionData(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
  classDirectories.setFrom(files(classDirectories.files.map {
    fileTree(it).matching {
      exclude("**/generated/**")
    }
  }))
}

// JSpecify null-safety enforcement via NullAway (an Error Prone plugin).
// The codebase is @NullMarked at the package level, so references are non-null
// by default; NullAway fails the build on any nullness violation in production code.
// onlyNullMarked: analysis scope is driven solely by @NullMarked (the JSpecify-native
// model), rather than matching a package prefix that would also catch generated contracts.
nullaway {
    onlyNullMarked = true
    jspecifyMode = true
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        // Run Error Prone's default checks; keep generated sources (MapStruct/Lombok) quiet.
        disableWarningsInGeneratedCode = true
        nullaway {
            error()
            // MapStruct impls are @Generated; don't hold generated code to the null contract.
            treatGeneratedAsUnannotated = true
            // Spring Data JDBC populates @Column-mapped fields after construction, so the
            // no-arg constructor legitimately leaves them unset — exclude them from init checks.
            excludedFieldAnnotations.add("org.springframework.data.relational.core.mapping.Column")
        }
    }
    // Tests aren't null-marked; only police production code.
    if (name != "compileJava") {
        options.errorprone.enabled = false
    }
}

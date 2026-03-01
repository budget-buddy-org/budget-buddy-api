import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
  java
  id("org.springframework.boot") version "4.0.3"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.openapi.generator") version "7.3.0"
}

group = "com.budget.buddy"
version = "0.0.1-SNAPSHOT"
description = "API for Budget Buddy App"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
}

dependencies {
  val mapstructVersion = "1.6.3"
  val lombokVersion = "1.18.42"
  val jjwtVersion = "0.13.0"

  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-h2console")
  implementation("org.springdoc:springdoc-openapi-starter-common:3.0.1")
  implementation("org.openapitools:jackson-databind-nullable:0.2.9")
  implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")

  compileOnly("org.projectlombok:lombok:${lombokVersion}")
  compileOnly("org.mapstruct:mapstruct:${mapstructVersion}")

  runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")
  runtimeOnly("com.h2database:h2:2.4.240")
  runtimeOnly("org.postgresql:postgresql:42.7.10")

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  developmentOnly("org.springframework.boot:spring-boot-docker-compose")

  annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
  annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")
  annotationProcessor("org.projectlombok:lombok:$lombokVersion")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.assertj:assertj-core")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

// OpenAPI Generator configuration
tasks.register<GenerateTask>("openapi") {
  generatorName.set("spring")
  inputSpec.set("$rootDir/src/main/resources/openapi.yaml")
  outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
  apiPackage.set("com.budget.buddy.budget_buddy_api.api")
  modelPackage.set("com.budget.buddy.budget_buddy_api.model")
  configOptions.set(
    mapOf(
      "useSpringBoot4" to "true",
      "generateApis" to "true",
      "generateModels" to "true",
      "generateSupportingFiles" to "false",
      "useTags" to "true",
      "interfaceOnly" to "true",
      "skipOperationExample" to "true",
      "sourceFolder" to "src/main/java",
      "useJakartaEe" to "true"
    )
  )
}

// Add generated sources to sourceSet
sourceSets {
  main {
    java {
      srcDir(layout.buildDirectory.dir("generated/src/main/java"))
    }
  }
}

// Ensure openapi task runs before compilation
tasks.compileJava {
  dependsOn("openapi")
}

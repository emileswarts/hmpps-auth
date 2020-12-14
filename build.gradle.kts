plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "2.1.0"
  kotlin("plugin.spring") version "1.4.21"
  kotlin("plugin.jpa") version "1.4.21"
  id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

repositories {
  maven("https://dl.bintray.com/gov-uk-notify/maven")
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.security:spring-security-jwt:1.1.1.RELEASE")
  implementation("org.springframework.security.oauth:spring-security-oauth2:2.5.0.RELEASE")
  implementation("io.jsonwebtoken:jjwt:0.9.1")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.sun.xml.bind:jaxb-impl:2.3.3")
  implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
  implementation("javax.activation:activation:1.1.1")

  implementation("javax.transaction:javax.transaction-api:1.3")

  implementation("io.springfox:springfox-swagger2:2.9.2")
  implementation("io.springfox:springfox-swagger-ui:2.9.2")

  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.5.1")
  implementation("uk.gov.service.notify:notifications-java-client:3.17.0-RELEASE")

  implementation("org.flywaydb:flyway-core:6.5.6")
  implementation("com.zaxxer:HikariCP:3.4.5")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.microsoft.sqlserver:mssql-jdbc:8.4.1.jre11")
  implementation("io.swagger:swagger-core:1.6.2")

  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.springframework.boot:spring-boot-devtools")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.8.0.0")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")

  testImplementation("org.gebish:geb-core:3.4.1")
  testImplementation("org.gebish:geb-spock:3.4.1")
  testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-firefox-driver:3.141.59")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.slf4j:slf4j-api:1.7.30")
  testImplementation("com.auth0:java-jwt:3.11.0")

  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.21.0")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:4.5.1")
  testImplementation("org.fluentlenium:fluentlenium-assertj:4.5.1")
  testImplementation("io.swagger.parser.v3:swagger-parser-v2-converter:2.0.23")
}

tasks {
  compileKotlin {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjvm-default=all")
    }
  }
  test {
    useJUnitPlatform()
    exclude("**/integration/*")
  }

  val testIntegration by registering(Test::class) {
    systemProperty(
      "fluentlenium.capabilities",
      """{"chromeOptions": {"args": ["headless","disable-gpu","disable-extensions","no-sandbox","disable-application-cache"]}}"""
    )
    useJUnitPlatform()
    include("uk/gov/justice/digital/hmpps/oauth2server/integration/*")
    // Note that java options set here would be overridden by _JAVA_OPTIONS in config.yml
  }
}

tasks.named("check") {
  dependsOn(":ktlintCheck")
}

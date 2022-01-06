plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.16"
  kotlin("plugin.spring") version "1.5.31"
  kotlin("plugin.jpa") version "1.5.31"
}

dependencyCheck {
  suppressionFiles.add("auth-suppressions.xml")
}

// added specifically due to thymeleaf@3.0.12.RELEASE and CVE-2021-43466 - remove when update included in spring-boot
ext["thymeleaf.version"] = "3.0.13.RELEASE"
ext["log4j2.version"] = "2.17.1"
ext["netty.version"] = "4.1.72.Final"
ext["logback.version"] = "1.2.10"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.security:spring-security-jwt:1.1.1.RELEASE")
  implementation("org.bouncycastle:bcpkix-jdk15on:1.69")

  implementation("org.springframework.security.oauth:spring-security-oauth2:2.5.1.RELEASE")
  implementation("io.jsonwebtoken:jjwt:0.9.1")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.session:spring-session-jdbc")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.sun.xml.bind:jaxb-impl:2.3.3")
  implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
  implementation("javax.activation:activation:1.1.1")

  implementation("javax.transaction:javax.transaction-api:1.3")

  implementation("io.springfox:springfox-boot-starter:3.0.0")
  implementation("io.swagger:swagger-core:1.6.2")

  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")
  implementation("uk.gov.service.notify:notifications-java-client:3.17.2-RELEASE")

  implementation("org.flywaydb:flyway-core:7.15.0")
  implementation("com.zaxxer:HikariCP:4.0.3")
  implementation("org.apache.commons:commons-text:1.9")
  implementation("com.microsoft.sqlserver:mssql-jdbc:9.4.0.jre16")

  runtimeOnly("com.h2database:h2:1.4.200")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.12.0.0")
  runtimeOnly("org.postgresql:postgresql:42.3.0")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")

  testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-firefox-driver:3.141.59")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.slf4j:slf4j-api:1.7.32")
  testImplementation("com.auth0:java-jwt:3.18.2")

  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.28.0")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:4.8.0")
  testImplementation("org.fluentlenium:fluentlenium-assertj:4.8.0")
  testImplementation("io.swagger.parser.v3:swagger-parser-v2-converter:2.0.27")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjvm-default=all")
      jvmTarget = "16"
    }
  }
  test {
    useJUnitPlatform()
    exclude("**/integration/*")
  }

  val testIntegration by registering(Test::class) {
    project.logger.lifecycle("Test paths provided:")
    project.logger.lifecycle(project.properties["ciTestFilter"].toString())
    if (project.hasProperty("ciTestFilter")) {
      val ciTestFilter = project.properties["ciTestFilter"].toString().split(System.lineSeparator())
      ciTestFilter.forEach {
        val testToRun = it.replace("src/test/kotlin/uk/gov/justice/digital/hmpps/oauth2server/integration/", "**/").replace(".kt", ".class")
        project.logger.lifecycle("testToRun: {}", testToRun)
        include(testToRun)
      }
    }

    systemProperty(
      "fluentlenium.capabilities",
      """{"chromeOptions": {"args": ["headless","disable-gpu","disable-extensions","no-sandbox","disable-application-cache"]}}"""
    )
    useJUnitPlatform()
    // Note that java options set here would be overridden by _JAVA_OPTIONS in config.yml
  }
}

tasks.named("check") {
  dependsOn(":ktlintCheck")
}

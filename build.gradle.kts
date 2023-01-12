plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.8.1-beta-1"
  kotlin("plugin.spring") version "1.8.0"
  kotlin("plugin.jpa") version "1.8.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencyCheck {
  suppressionFiles.add("auth-suppressions.xml")
}

// Temporarily kept selenium version at 4.1.4 as tests fail using 4.5.3
val seleniumVersion by extra("4.1.4")

ext["selenium.version"] = "4.1.2"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.security:spring-security-jwt:1.1.1.RELEASE")
  implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

  implementation("org.springframework.security.oauth:spring-security-oauth2:2.5.2.RELEASE")
  implementation("io.jsonwebtoken:jjwt:0.9.1")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.hibernate:hibernate-core")

  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.session:spring-session-jdbc")

  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("com.sun.xml.bind:jaxb-impl:4.0.1")
  implementation("com.sun.xml.bind:jaxb-core:4.0.1")
  implementation("javax.activation:activation:1.1.1")

  implementation("javax.transaction:javax.transaction-api:1.3")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.14")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.14")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.14")

  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.1.0")
  // thymeleaf-layout-dialect:3.1.0 uses groovy 4.0.0 which contains CVE-2020-17521 https://nvd.nist.gov/vuln/detail/CVE-2020-17521
  // org.apache.groovy:groovy can be removed when thymeleaf-layout-dialect is updated and include groovy:4.0.2 or later
  implementation("org.apache.groovy:groovy:4.0.7")
  implementation("uk.gov.service.notify:notifications-java-client:3.19.0-RELEASE")

  implementation("org.flywaydb:flyway-core")
  implementation("com.zaxxer:HikariCP:5.0.1")
  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("com.veracode.annotation:VeracodeAnnotations:1.2.1")

  runtimeOnly("com.h2database:h2:2.1.214")
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  runtimeOnly("org.postgresql:postgresql:42.5.1")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")

  testImplementation("org.seleniumhq.selenium:selenium-support:$seleniumVersion")
  testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion")
  testImplementation("org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion")
  testImplementation("io.github.http-builder-ng:http-builder-ng-apache:1.0.4")

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.slf4j:slf4j-api:2.0.6")
  testImplementation("com.auth0:java-jwt:4.2.1")

  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.36.0")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:5.0.4")
  testImplementation("org.fluentlenium:fluentlenium-assertj:5.0.4")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(18))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjvm-default=all")
      jvmTarget = "18"
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

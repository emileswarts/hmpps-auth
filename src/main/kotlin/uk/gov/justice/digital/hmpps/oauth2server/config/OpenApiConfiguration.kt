package uk.gov.justice.digital.hmpps.oauth2server.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://sign-in.hmpps.service.justice.gov.uk/auth").description("Prod"),
        Server().url("https://sign-in-preprod.hmpps.service.justice.gov.uk/auth").description("PreProd"),
        Server().url("https://sign-in-dev.hmpps.service.justice.gov.uk/auth").description("Development"),
        Server().url("http://localhost:9090/auth").description("Local"),
      )
    )
    .info(
      Info().title("HMPPS Auth")
        .version(version)
        .description("HMPPS Auth API")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk"))
    )
}

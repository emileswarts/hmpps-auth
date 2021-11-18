package uk.gov.justice.digital.hmpps.oauth2server.config

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class NomisWebClientConfiguration(appContext: ApplicationContext) :
  AbstractWebClientConfiguration(appContext, "nomis") {

  @Bean("nomisClientRegistration")
  fun getNomisClientRegistration(): ClientRegistration = getClientRegistration()

  @Bean("nomisWebClient")
  fun nomisWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager
  ): WebClient = getWebClient(builder, authorizedClientManager)

  @Bean("nomisUserWebClient")
  fun nomisUserWebClient(
    builder: WebClient.Builder,
  ): WebClient = getWebClientWithCurrentUserToken(builder)

  @Bean("nomisHealthWebClient")
  fun nomisHealthWebClient(builder: WebClient.Builder): WebClient = getHealthWebClient(builder)
}

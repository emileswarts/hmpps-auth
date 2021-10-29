package uk.gov.justice.digital.hmpps.oauth2server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class DeliusWebClientConfiguration(appContext: ApplicationContext) :
  AbstractWebClientConfiguration(appContext, "delius") {

  @Bean("deliusClientRegistration")
  fun getDeliusClientRegistration(): ClientRegistration = getClientRegistration()

  @Bean("deliusWebClient")
  fun deliusWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager
  ): WebClient = getWebClient(builder, authorizedClientManager, "/secure")

  @Bean("deliusHealthWebClient")
  fun deliusHealthWebClient(builder: WebClient.Builder): WebClient = getHealthWebClient(builder)
}

@Suppress("ConfigurationProperties", "ConfigurationProperties")
@ConstructorBinding
@ConfigurationProperties("delius.roles")
open class DeliusRoleMappings(val mappings: Map<String, List<String>>)

package uk.gov.justice.digital.hmpps.oauth2server.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.hibernate.validator.constraints.URL
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import java.time.Duration

abstract class AbstractWebClientConfiguration(appContext: ApplicationContext, private val clientId: String) {
  private val environment = appContext.environment

  fun getClientRegistration(): ClientRegistration = ClientRegistration.withRegistrationId(clientId)
    .clientName(clientId)
    .clientId(environment.getProperty("$clientId.client.client-id"))
    .clientSecret(environment.getProperty("$clientId.client.client-secret"))
    .clientSecret(environment.getProperty("$clientId.client.client-secret"))
    .tokenUri(environment.getProperty("$clientId.client.access-token-uri"))
    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
    .build()

  fun getWebClient(
    builder: WebClient.Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    prefix: String = ""
  ): WebClient {
    val oauth2 = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2.setDefaultClientRegistrationId(clientId)

    val apiTimeout = environment.getProperty("$clientId.endpoint.timeout", Duration::class.java)
    val endpointUrl = environment.getProperty("$clientId.endpoint.url", String::class.java)

    return builder
      .baseUrl("${endpointUrl}$prefix")
      .apply(oauth2.oauth2Configuration())
      .clientConnector(
        getClientConnectorWithTimeouts(
          apiTimeout, apiTimeout,
          endpointUrl, environment.getProperty("$clientId.enabled", Boolean::class.java)
        )
      )
      .build()
  }

  fun getHealthWebClient(builder: WebClient.Builder): WebClient {
    val endpointUrl = environment.getProperty("$clientId.endpoint.url", String::class.java)
    val healthTimeout = environment.getProperty("$clientId.health.timeout", Duration::class.java)

    return builder
      .baseUrl(endpointUrl)
      .clientConnector(
        getClientConnectorWithTimeouts(
          healthTimeout, healthTimeout, endpointUrl,
          environment.getProperty("$clientId.enabled", Boolean::class.java)
        )
      )
      .build()
  }

  private fun getClientConnectorWithTimeouts(
    connectTimeout: Duration,
    readTimeout: Duration,
    url: @URL String,
    warmup: Boolean,
  ): ClientHttpConnector {
    val httpClient = HttpClient.create()
    if (warmup) httpClient.warmupWithHealthPing(url)
    return ReactorClientHttpConnector(
      httpClient
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout.toMillis().toInt())
        .doOnConnected { connection: Connection ->
          connection
            .addHandlerLast(ReadTimeoutHandler(readTimeout.toSeconds().toInt()))
        }
    )
  }

  private fun HttpClient.warmupWithHealthPing(baseUrl: String): HttpClient {
    log.info("Warming up web client for {}", baseUrl)
    warmup().block()
    log.info("Warming up web client for {} halfway through, now calling health ping", baseUrl)
    try {
      baseUrl("$baseUrl/health/ping").get().response().block(Duration.ofSeconds(30))
    } catch (e: RuntimeException) {
      log.error("Caught exception during warm up, carrying on regardless", e)
    }
    log.info("Warming up web client completed for {}", baseUrl)
    return this
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

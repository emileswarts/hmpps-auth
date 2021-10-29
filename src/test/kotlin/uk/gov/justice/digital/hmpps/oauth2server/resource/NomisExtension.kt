package uk.gov.justice.digital.hmpps.oauth2server.resource

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor

class NomisExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback, TestInstancePostProcessor {
  companion object {
    @JvmField
    val nomisApi = NomisApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    nomisApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    nomisApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    nomisApi.stop()
  }

  override fun postProcessTestInstance(testInstance: Any?, context: ExtensionContext?) {
    try {
      val field = testInstance?.javaClass?.getField("nomisApi")
      field?.set(testInstance, nomisApi)
    } catch (e: NoSuchFieldException) {
    }
  }
}

class NomisApiMockServer : WireMockServer(
  wireMockConfig()
    .port(8101)
    .usingFilesUnderClasspath("nomis")
    .extensions(ResponseTemplateTransformer(false))
)

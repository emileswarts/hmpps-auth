package uk.gov.justice.digital.hmpps.oauth2server.config

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ThymeleafConfig {
  @Bean
  fun layoutDialect() = LayoutDialect()
}

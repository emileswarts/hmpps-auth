package uk.gov.justice.digital.hmpps.oauth2server.utils
import com.veracode.annotation.CRLFCleanser

@CRLFCleanser
fun String.removeAllCrLf() =
  replace("\r", "")
    .replace("\n", "")

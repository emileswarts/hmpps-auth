package uk.gov.justice.digital.hmpps.oauth2server.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PasswordGeneratorTest() {
  private val passwordGenerator = PasswordGenerator(60)

  private val generatedPassword = passwordGenerator.generatePassword()
  private val generatedPasswordCountMap = checkString(generatedPassword)

  @Test
  fun `password should be generated with at least 2 lowercase letters`() {
    assertThat(generatedPasswordCountMap["lowerCaseCount"]).isGreaterThan(2)
  }

  @Test
  fun `password should be generated with at least 2 uppercase letters`() {
    assertThat(generatedPasswordCountMap["upperCaseCount"]).isGreaterThan(2)
  }

  @Test
  fun `password should be generated with at least 2 numbers`() {
    assertThat(generatedPasswordCountMap["numberCount"]).isGreaterThan(2)
  }

  @Test
  fun `password should be generated with at least 4 special characters`() {
    assertThat(generatedPasswordCountMap["specialCharacterCount"]).isGreaterThan(3)
  }

  @Test
  fun `password should be generated but not contain single quote`() {
    assertThat(generatedPassword).doesNotContain("'")
  }

  @Test
  fun `password should be generated but not contain double quotes`() {
    assertThat(generatedPassword).doesNotContain("'")
  }

  @Test
  fun `password should be generated but not contain #`() {
    assertThat(generatedPassword).doesNotContain("'")
  }

  @Test
  fun `password should be generated but not contain forward slash`() {
    assertThat(generatedPassword).doesNotContain("/")
  }

  @Test
  fun `password should be 60 characters long`() {
    assertThat(generatedPassword.length).isEqualTo(60)
  }

  private fun checkString(input: String): Map<String, Int> {
    val specialChars = "~`!@#$%^&*()-_=+\\|[{]};:'\",<.>/?"
    var currentCharacter: Char
    var lowerCaseCount = 0
    var upperCaseCount = 0
    var numberCount = 0
    var specialCharacterCount = 0
    input.forEach {
      currentCharacter = it
      when {
        Character.isDigit(currentCharacter) -> {
          numberCount = numberCount.plus(1)
        }
        Character.isUpperCase(currentCharacter) -> {
          upperCaseCount = upperCaseCount.plus(1)
        }
        Character.isLowerCase(currentCharacter) -> {
          lowerCaseCount = lowerCaseCount.plus(1)
        }
        specialChars.contains(currentCharacter.toString()) -> {
          specialCharacterCount = specialCharacterCount.plus(1)
        }
      }
    }
    return mapOf(
      "numberCount" to numberCount,
      "upperCaseCount" to upperCaseCount,
      "lowerCaseCount" to lowerCaseCount,
      "specialCharacterCount" to specialCharacterCount
    )
  }
}

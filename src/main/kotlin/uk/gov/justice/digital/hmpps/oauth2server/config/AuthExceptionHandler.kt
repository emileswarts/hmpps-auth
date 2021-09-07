@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.oauth2server.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.provider.NoSuchClientException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService.AuthUserGroupException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService.AuthUserGroupManagerException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserGroupService.AuthUserLastGroupException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService.AuthUserRoleException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService.AuthUserRoleExistsException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.ChildGroupExistsException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.GroupExistsException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.GroupHasChildGroupException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.GroupsService.GroupNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.maintain.RolesService.RoleNotFoundException
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck.AuthGroupRelationshipException
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck.AuthUserGroupRelationshipException
import uk.gov.justice.digital.hmpps.oauth2server.service.DuplicateClientsException
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.ValidEmailException

@RestControllerAdvice
class AuthExceptionHandler {
  @ExceptionHandler(UsernameNotFoundException::class)
  fun handleNotFoundException(e: UsernameNotFoundException): ResponseEntity<ErrorDetail> {
    log.debug("Username not found exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorDetail(HttpStatus.NOT_FOUND.reasonPhrase, e.message ?: "Error message not set", "username"))
  }

  @ExceptionHandler(AuthUserRoleExistsException::class)
  fun handleAuthUserRoleExistsException(e: AuthUserRoleExistsException): ResponseEntity<ErrorDetail> {
    log.debug("Auth user role exists exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", e.field))
  }

  @ExceptionHandler(AuthUserGroupRelationshipException::class)
  fun handleAuthUserGroupRelationshipException(e: AuthUserGroupRelationshipException): ResponseEntity<ErrorDetail> {
    log.debug("Auth user group relationship exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "username"))
  }

  @ExceptionHandler(GroupNotFoundException::class)
  fun handleGroupNotFoundException(e: GroupNotFoundException): ResponseEntity<ErrorDetail> {
    log.debug("Username not found exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorDetail(HttpStatus.NOT_FOUND.reasonPhrase, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(AuthGroupRelationshipException::class)
  fun handleAuthGroupRelationshipException(e: AuthGroupRelationshipException): ResponseEntity<ErrorDetail> {
    log.debug("Auth maintain group relationship exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(AuthUserRoleException::class)
  fun handleAuthUserRoleException(e: AuthUserRoleException): ResponseEntity<ErrorDetail> {
    log.debug("Auth user role exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", e.field))
  }

  @ExceptionHandler(GroupHasChildGroupException::class)
  fun handleGroupHasChildGroupException(e: GroupHasChildGroupException): ResponseEntity<ErrorDetail> {
    log.debug("Group has children exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(ChildGroupExistsException::class)
  fun handleChildGroupExistsException(e: ChildGroupExistsException): ResponseEntity<ErrorDetail> {
    log.debug("Child group exists exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(GroupExistsException::class)
  fun handleGroupExistsException(e: GroupExistsException): ResponseEntity<ErrorDetail> {
    log.debug("Group exists exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(DuplicateClientsException::class)
  fun handleDuplicateClientsException(e: DuplicateClientsException): ResponseEntity<ErrorDetail> {
    log.debug("Duplicate client exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorDetail("MaxDuplicateReached", e.message ?: "Error message not set", "client"))
  }

  @ExceptionHandler(NoSuchClientException::class)
  fun handleNoSuchClientException(e: NoSuchClientException): ResponseEntity<ErrorDetail> {
    log.debug("No such client exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorDetail(HttpStatus.NOT_FOUND.reasonPhrase, e.message ?: "No client with requested id", "client"))
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorDetail> {
    log.debug("MethodArgumentNotValidException exception caught: {}", e.message)
    val field = if (e.allErrors.size > 0) e.allErrors[0].objectName else "none"
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorDetail(HttpStatus.BAD_REQUEST.reasonPhrase, e.message ?: "Error message not set", field))
  }

  @ExceptionHandler(AuthUserGroupException::class)
  fun handleAuthUserGroupException(e: AuthUserGroupException): ResponseEntity<ErrorDetail> {
    log.debug("Auth user group exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(AuthUserGroupManagerException::class)
  fun handleAuthUserGroupManagerException(e: AuthUserGroupManagerException): ResponseEntity<ErrorDetail> {
    log.debug("Auth user group exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(AuthUserLastGroupException::class)
  fun handleAuthUserLastGroupException(e: AuthUserLastGroupException): ResponseEntity<ErrorDetail> {
    log.debug("Auth user group exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ErrorDetail(e.errorCode, e.message ?: "Error message not set", "group"))
  }

  @ExceptionHandler(ValidEmailException::class)
  fun handleAuthUserLastGroupException(e: ValidEmailException): ResponseEntity<ErrorDetail> {
    log.info("Amend user email exception caught: {}", e.message)
    return ResponseEntity.badRequest()
      .body(ErrorDetail("email.${e.reason}", "Email address failed validation", "email"))
  }

  @ExceptionHandler(RoleNotFoundException::class)
  fun handleRoleNotFoundException(e: RoleNotFoundException): ResponseEntity<ErrorDetail> {
    log.debug("Role not found exception caught: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorDetail(HttpStatus.NOT_FOUND.reasonPhrase, e.message ?: "Error message not set", "role"))
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

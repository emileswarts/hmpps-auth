package uk.gov.justice.digital.hmpps.oauth2server.maintain;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority;
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.RoleRepository;
import uk.gov.justice.digital.hmpps.oauth2server.auth.repository.UserEmailRepository;
import uk.gov.justice.digital.hmpps.oauth2server.security.MaintainUserCheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional(transactionManager = "authTransactionManager", readOnly = true)
@AllArgsConstructor
public class AuthUserRoleService {

    private final UserEmailRepository userEmailRepository;
    private final RoleRepository roleRepository;
    private final TelemetryClient telemetryClient;
    private final MaintainUserCheck maintainUserCheck;

    @Transactional(transactionManager = "authTransactionManager")
    public void addRole(final String username, final String roleCode, final String loggedInUser, final Collection<? extends GrantedAuthority> authorities) throws AuthUserRoleException, MaintainUserCheck.AuthUserGroupRelationshipException {
        final var roleFormatted = formatRole(roleCode);

        // already checked that user exists
        final var userEmail = userEmailRepository.findByUsernameAndMasterIsTrue(username).orElseThrow();
        maintainUserCheck.ensureUserLoggedInUserRelationship(loggedInUser, authorities, userEmail);

        // check that role exists
        final var role = roleRepository.findByRoleCode(roleFormatted).orElseThrow(() -> new AuthUserRoleException("role", "notfound"));

        if (userEmail.getAuthorities().contains(role)) {
            throw new AuthUserRoleExistsException();
        }

        if (!getAssignableRoles(username, authorities).contains(role)) {
            throw new AuthUserRoleException("role", "invalid");
        }

        log.info("Adding role {} to user {}", roleFormatted, username);
        userEmail.getAuthorities().add(role);
        telemetryClient.trackEvent("AuthUserRoleAddSuccess", Map.of("username", username, "role", roleFormatted, "admin", loggedInUser), null);
    }

    @Transactional(transactionManager = "authTransactionManager")
    public void removeRole(final String username, final String roleCode, final String loggedInUser, final Collection<? extends GrantedAuthority> authorities) throws AuthUserRoleException, MaintainUserCheck.AuthUserGroupRelationshipException {
        // already checked that user exists
        final var userEmail = userEmailRepository.findByUsernameAndMasterIsTrue(username).orElseThrow();

        // check that the logged in user has permission to modify user
        maintainUserCheck.ensureUserLoggedInUserRelationship(loggedInUser, authorities, userEmail);

        final var roleFormatted = formatRole(roleCode);
        final var role = roleRepository.findByRoleCode(roleFormatted).orElseThrow(() -> new AuthUserRoleException("role", "notfound"));

        if (!userEmail.getAuthorities().contains(role)) {
            throw new AuthUserRoleException("role", "missing");
        }

        if (!getAssignableRoles(username, authorities).contains(role)) {
            throw new AuthUserRoleException("role", "invalid");
        }

        log.info("Removing role {} from user {}", roleFormatted, username);
        userEmail.getAuthorities().removeIf(role::equals);
        telemetryClient.trackEvent("AuthUserRoleRemoveSuccess", Map.of("username", username, "role", roleFormatted, "admin", loggedInUser), null);
    }

    private String formatRole(final String role) {
        return Authority.removeRolePrefixIfNecessary(StringUtils.upperCase(StringUtils.trim(role)));
    }

    public List<Authority> getAllRoles() {
        return roleRepository.findAllByOrderByRoleName();
    }

    public List<Authority> getAssignableRoles(final String username, final Collection<? extends GrantedAuthority> authorities) {
        if (MaintainUserCheck.canMaintainAuthUsers(authorities)) {
            // only allow oauth admins to see that role
            return getAllRoles().stream().filter(r -> !"OAUTH_ADMIN".equals(r.getRoleCode()) || canAddAuthClients(authorities)).collect(Collectors.toList());
        }
        // otherwise they can assign all roles that can be assigned to any of their groups
        return roleRepository.findByGroupAssignableRolesForUsername(username);
    }

    public static class AuthUserRoleExistsException extends AuthUserRoleException {
        public AuthUserRoleExistsException() {
            super("role", "exists");
        }
    }

    private static boolean canAddAuthClients(final Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_OAUTH_ADMIN"::equals);
    }

    @Getter
    public static class AuthUserRoleException extends Exception {
        private final String errorCode;
        private final String field;

        public AuthUserRoleException(final String field, final String errorCode) {
            super(String.format("Add role failed for field %s with reason: %s", field, errorCode));

            this.field = field;
            this.errorCode = errorCode;
        }
    }
}

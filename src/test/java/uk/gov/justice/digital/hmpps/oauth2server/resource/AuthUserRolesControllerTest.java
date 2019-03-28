package uk.gov.justice.digital.hmpps.oauth2server.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.Authority;
import uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserEmail;
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService;
import uk.gov.justice.digital.hmpps.oauth2server.maintain.AuthUserRoleService.AuthUserRoleException;
import uk.gov.justice.digital.hmpps.oauth2server.model.ErrorDetail;
import uk.gov.justice.digital.hmpps.oauth2server.model.UserRole;
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthUserRolesControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private AuthUserRoleService authUserRoleService;

    private AuthUserRolesController authUserRolesController;

    @Before
    public void setUp() {
        authUserRolesController = new AuthUserRolesController(userService, authUserRoleService);
    }

    @Test
    public void roles_userNotFound() {
        final var responseEntity = authUserRolesController.roles("bob");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(404);
        assertThat(responseEntity.getBody()).isEqualTo(new ErrorDetail("Not Found", "Account for username bob not found"));
    }

    @Test
    public void roles_success() {
        when(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(getAuthUser()));
        final var responseEntity = authUserRolesController.roles("joe");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        //noinspection unchecked
        assertThat(((Set) responseEntity.getBody())).containsOnly(new UserRole("FRED"), new UserRole("JOE"));
    }

    @Test
    public void addRole_userNotFound() {
        final var responseEntity = authUserRolesController.addRole("bob", "role");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(404);
        assertThat(responseEntity.getBody()).isEqualTo(new ErrorDetail("Not Found", "Account for username bob not found"));
    }

    @Test
    public void addRole_success() throws AuthUserRoleException {
        when(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(getAuthUser()));
        final var responseEntity = authUserRolesController.addRole("someuser", "role");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(204);
        verify(authUserRoleService).addRole("USER", "ROLE_ROLE");
    }

    @Test
    public void addRole_conflict() {
        when(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(getAuthUser()));
        final var responseEntity = authUserRolesController.addRole("someuser", "joe");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(409);
    }

    @Test
    public void addRole_validation() throws AuthUserRoleException {
        when(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(getAuthUser()));

        doThrow(new AuthUserRoleException("role", "error")).when(authUserRoleService).addRole(anyString(), anyString());
        final var responseEntity = authUserRolesController.addRole("someuser", "harry");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
        assertThat(responseEntity.getBody()).isEqualTo(new ErrorDetail("role.error", "role failed validation"));
    }

    @Test
    public void removeRole_userNotFound() {
        final var responseEntity = authUserRolesController.removeRole("bob", "role");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(404);
        assertThat(responseEntity.getBody()).isEqualTo(new ErrorDetail("Not Found", "Account for username bob not found"));
    }

    @Test
    public void removeRole_success() {
        when(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(getAuthUser()));
        final var responseEntity = authUserRolesController.removeRole("someuser", "joe");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(204);
        verify(authUserRoleService).removeRole("USER", "ROLE_JOE");
    }

    @Test
    public void removeRole_roleMissing() {
        when(userService.getAuthUserByUsername(anyString())).thenReturn(Optional.of(getAuthUser()));
        final var responseEntity = authUserRolesController.removeRole("someuser", "harry");
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }

    private UserEmail getAuthUser() {
        final var user = new UserEmail("USER", "email", true, false);
        user.setAuthorities(Set.of(new Authority("JOE"), new Authority("FRED")));
        return user;
    }
}

package uk.gov.justice.digital.hmpps.oauth2server.auth.model;

import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import uk.gov.justice.digital.hmpps.oauth2server.security.UserPersonDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "USER_EMAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"username"})
@Builder
public class User implements UserPersonDetails, CredentialsContainer {

    @Id
    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * Whether we are masters of this user record here in auth
     */
    @Column(name = "master", nullable = false)
    private boolean master;

    /**
     * Used for NOMIS accounts to force change password so that they don't get locked out due to not changing password
     */
    @Column(name = "password_expiry")
    @Builder.Default
    private LocalDateTime passwordExpiry = LocalDateTime.now();

    @Column(name = "last_logged_in")
    @Builder.Default
    private LocalDateTime lastLoggedIn = LocalDateTime.now();

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "username")
    @PrimaryKeyJoinColumn
    private Person person;

    @OneToMany(fetch = EAGER)
    @JoinTable(name = "user_email_roles",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Authority> authorities = new HashSet<>();

    @OneToMany
    @JoinTable(name = "user_email_groups",
            joinColumns = @JoinColumn(name = "useremail_username"),
            inverseJoinColumns = @JoinColumn(name = "groups_group_id"))
    @Builder.Default
    private Set<Group> groups = new HashSet<>();

    public static User of(final String username) {
        return User.builder().username(username).build();
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return passwordExpiry.isAfter(LocalDateTime.now());
    }

    @Override
    public String getName() {
        return person != null ? person.getName() : username;
    }

    @Override
    public String getFirstName() {
        return person != null ? person.getFirstName() : username;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public String getAuthSource() {
        return "auth";
    }
}
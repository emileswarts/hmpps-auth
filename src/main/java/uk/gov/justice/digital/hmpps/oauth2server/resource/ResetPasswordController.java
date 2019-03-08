package uk.gov.justice.digital.hmpps.oauth2server.resource;

import com.microsoft.applicationinsights.TelemetryClient;
import com.weddini.throttling.ThrottlingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.justice.digital.hmpps.oauth2server.model.Context;
import uk.gov.justice.digital.hmpps.oauth2server.security.UserService;
import uk.gov.justice.digital.hmpps.oauth2server.utils.IpAddressHelper;
import uk.gov.justice.digital.hmpps.oauth2server.verify.ResetPasswordService;
import uk.gov.justice.digital.hmpps.oauth2server.verify.ResetPasswordServiceImpl.NotificationClientRuntimeException;
import uk.gov.justice.digital.hmpps.oauth2server.verify.TokenService;
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService;
import uk.gov.justice.digital.hmpps.oauth2server.verify.VerifyEmailService.VerifyEmailException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

import static uk.gov.justice.digital.hmpps.oauth2server.auth.model.UserToken.TokenType.RESET;

@Slf4j
@Controller
@Validated
public class ResetPasswordController extends AbstractPasswordController {
    private final ResetPasswordService resetPasswordService;
    private final TokenService tokenService;
    private final VerifyEmailService verifyEmailService;
    private final TelemetryClient telemetryClient;
    private final boolean smokeTestEnabled;

    public ResetPasswordController(final ResetPasswordService resetPasswordService,
                                   final TokenService tokenService, final UserService userService,
                                   final VerifyEmailService verifyEmailService, final TelemetryClient telemetryClient, @Value("${application.smoketest.enabled}") final boolean smokeTestEnabled,
                                   final @Value("${application.authentication.blacklist}") Set<String> passwordBlacklist) {

        super(resetPasswordService, tokenService, userService, telemetryClient, "resetPassword", "setPassword", passwordBlacklist);
        this.resetPasswordService = resetPasswordService;
        this.tokenService = tokenService;
        this.verifyEmailService = verifyEmailService;
        this.telemetryClient = telemetryClient;
        this.smokeTestEnabled = smokeTestEnabled;
    }

    @GetMapping("/reset-password")
    public String resetPasswordRequest() {
        return "resetPassword";
    }

    @GetMapping("/reset-password-success")
    public String resetPasswordSuccess() {
        return "resetPasswordSuccess";
    }

    @PostMapping("/reset-password")
    public ModelAndView resetPasswordRequest(@RequestParam(required = false) final String usernameOrEmail,
                                             final HttpServletRequest request) {
        if (StringUtils.isBlank(usernameOrEmail)) {
            telemetryClient.trackEvent("ResetPasswordRequestFailure", Map.of("error", "missing"), null);
            return new ModelAndView("resetPassword", "error", "missing");
        }

        if (StringUtils.contains(usernameOrEmail, "@")) {
            try {
                verifyEmailService.validateEmailAddress(usernameOrEmail);
            } catch (final VerifyEmailException e) {
                log.info("Validation failed for reset password email address due to {}", e.getReason());
                telemetryClient.trackEvent("VerifyEmailRequestFailure", Map.of("email", usernameOrEmail, "reason", "email." + e.getReason()), null);
                return new ModelAndView("resetPassword", Map.of("error", "email." + e.getReason(), "usernameOrEmail", usernameOrEmail));
            }
        }

        try {
            final var resetLink = resetPasswordService.requestResetPassword(usernameOrEmail, request.getRequestURL().append("-confirm?token=").toString());
            final var modelAndView = new ModelAndView("resetPasswordSent");
            if (resetLink.isPresent()) {
                log.info("Reset password request success for {}", usernameOrEmail);
                telemetryClient.trackEvent("ResetPasswordRequestSuccess", Map.of("username", usernameOrEmail), null);
                if (smokeTestEnabled) {
                    modelAndView.addObject("resetLink", resetLink.get());
                }
            } else {
                log.info("Reset password request failed, no link provided for {}", usernameOrEmail);
                telemetryClient.trackEvent("ResetPasswordRequestFailure", Map.of("username", usernameOrEmail, "error", "nolink"), null);
                if (smokeTestEnabled) {
                    modelAndView.addObject("resetLinkMissing", true);
                }
            }
            return modelAndView;

        } catch (final NotificationClientRuntimeException e) {
            log.error("Failed to send reset password due to", e);
            telemetryClient.trackEvent("ResetPasswordRequestFailure",
                    Map.of("username", usernameOrEmail, "error", e.getClass().getSimpleName()), null);
            return new ModelAndView("resetPassword", "error", "other");
        } catch (final ThrottlingException e) {
            final var ip = IpAddressHelper.retrieveIpFromRemoteAddr(request);
            log.info("Reset password throttled request for {}", ip);
            telemetryClient.trackEvent("ResetPasswordRequestFailure",
                    Map.of("username", usernameOrEmail, "error", e.getClass().getSimpleName(), "remoteAddress", ip), null);
            return new ModelAndView("resetPassword", "error", "throttled");
        }
    }

    @GetMapping("/reset-password-confirm")
    public ModelAndView resetPasswordConfirm(@RequestParam final String token) {
        final var userTokenOptional = tokenService.checkToken(RESET, token);
        return userTokenOptional.map(s -> new ModelAndView("resetPassword", "error", s)).
                orElseGet(() -> createModelWithTokenAndAddIsAdmin(RESET, token, "setPassword"));
    }

    @PostMapping("/set-password")
    public ModelAndView setPassword(@RequestParam final String token,
                                    @RequestParam final String newPassword, @RequestParam final String confirmPassword,
                                    @RequestParam(required = false) final String context) {
        final var modelAndView = processSetPassword(RESET, token, newPassword, confirmPassword);
        final var licences = Context.get(context) == Context.LICENCES;

        return modelAndView.map(mv -> licences ? mv.addObject("context", context) : mv).orElse(
                new ModelAndView(licences ? "redirect:/initial-password-success" : "redirect:/reset-password-success"));
    }
}

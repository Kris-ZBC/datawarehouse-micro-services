package local.sop.datawarehouse.login.interfaceweb;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.login.application.api.LoginDirectory;
import local.sop.datawarehouse.login.application.api.dto.AuthenticateCmd;
import local.sop.datawarehouse.login.application.api.dto.AuthenticationResult;
import local.sop.datawarehouse.login.application.api.dto.CreateLoginCmd;
import local.sop.datawarehouse.login.application.api.dto.CreateSessionCmd;
import local.sop.datawarehouse.login.application.api.dto.CreatedLoginResult;
import local.sop.datawarehouse.login.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.application.api.dto.LogoutCmd;
import local.sop.datawarehouse.login.application.api.dto.SessionValidationResult;
import local.sop.datawarehouse.login.application.api.dto.UpdateLoginStatusCmd;
import local.sop.datawarehouse.login.application.api.dto.ValidateSessionCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@RestController
@RequestMapping("/internal/logins")
public class InternalLoginController {

    private static final Logger log =
        LoggerFactory.getLogger(InternalLoginController.class);

    private final LoginDirectory directory;

    InternalLoginController(LoginDirectory directory) {
        this.directory = directory;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreatedLoginResult> createLogin(
            @Valid @RequestBody CreateLoginCmd cmd) {
        CreatedLoginResult result = directory.createLogin(cmd);
        URI location = URI.create("/internal/logins/" + result.id());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
    }

    // CHANGED: was POST /sessions/login, calling the old combined
    // login(LoginCmd). Split into this (credential check only) and
    // createSession below — login-saga resolves role between the two.
    @PostMapping(
        path = "/sessions/authenticate",
        consumes = "application/json",
        produces = "application/json")
    public ResponseEntity<AuthenticationResult> authenticate(@Valid @RequestBody AuthenticateCmd cmd) {
        AuthenticationResult result = directory.authenticate(cmd);
        log.info("Authentication successful for user={}", result.username());
        return ResponseEntity.ok(result);
    }

    @PostMapping(
        path = "/sessions",
        consumes = "application/json",
        produces = "application/json")
    public ResponseEntity<LoginResult> createSession(@Valid @RequestBody CreateSessionCmd cmd) {
        LoginResult result = directory.createSession(cmd);
        log.info("Session created for user={}, role={}, tokenPrefix={}",
            result.username(),
            result.role(),
            result.sessionToken().substring(0, Math.min(8, result.sessionToken().length())));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(
        path = "/sessions/logout",
        consumes = "application/json")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutCmd cmd) {
        directory.logout(cmd);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
        path = "/sessions/validate",
        consumes = "application/json",
        produces = "application/json")
    public ResponseEntity<SessionValidationResult> validateSession(
            @Valid @RequestBody ValidateSessionCmd cmd) {
        SessionValidationResult result = directory.validateSession(cmd);
        if (!result.valid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/compensate")
    public ResponseEntity<ResponseCompensated> compensate(
            @RequestBody CompensateLoginCmd cmd) {
        ResponseCompensated result =
            directory.compensate(cmd.id(), getClass(), cmd.sagaState());
        return ResponseEntity.ok(result);
    }

    // NEW: general-purpose disable/re-activate — not tech-user-specific.
    @PostMapping(
        path = "/status",
        consumes = "application/json")
    public ResponseEntity<Void> updateLoginStatus(@Valid @RequestBody UpdateLoginStatusCmd cmd) {
        directory.updateLoginStatus(cmd);
        log.info("Login status updated for loginId={}, status={}", cmd.loginId(), cmd.status());
        return ResponseEntity.noContent().build();
    }

    public record CompensateLoginCmd(UUID id, SagaOutcome sagaState) {}
}
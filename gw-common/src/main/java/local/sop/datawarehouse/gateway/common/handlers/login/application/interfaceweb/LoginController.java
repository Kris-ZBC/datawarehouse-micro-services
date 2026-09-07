package local.sop.datawarehouse.gateway.common.handlers.login.application.interfaceweb;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
 
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import jakarta.validation.Valid;
import local.sop.datawarehouse.gateway.common.handlers.login.api.LoginDirectory;
import local.sop.datawarehouse.gateway.common.handlers.login.api.dto.LoginRequest;
import local.sop.datawarehouse.gateway.common.handlers.login.api.dto.LoginResponse;
import local.sop.datawarehouse.gateway.common.handlers.login.application.config.CookieProps;
import local.sop.datawarehouse.gateway.common.handlers.login.application.ports.LoginPort.LoginResult;
 
@RestController
@RequestMapping("/api/v1/common")
public class LoginController {
 
    private final LoginDirectory loginDirectory;
    private final CookieProps cookieProps;
 
    public LoginController(LoginDirectory loginDirectory, CookieProps cookieProps) {
        this.loginDirectory = loginDirectory;
        this.cookieProps = cookieProps;
    }
 
    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public CompletableFuture<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return CompletableFuture.supplyAsync(() -> loginDirectory.login(request))
                .thenApply(result -> {
                    ResponseCookie cookie = buildSessionCookie(result);
                    LoginResponse body = new LoginResponse(
                            result.loginId(), result.personRef(), result.username(), result.role(), result.expiresAt());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(body);
                });
    }
 
    @PostMapping(path = "/logout")
    public CompletableFuture<ResponseEntity<Void>> logout(
            @org.springframework.web.bind.annotation.CookieValue(name = "${sop.session-cookie.name:SOP_SESSION}", required = false) String sessionToken) {
        return CompletableFuture.supplyAsync(() -> {
            if (sessionToken != null) {
                loginDirectory.logout(sessionToken);
            }
            return null;
        }).thenApply(v -> ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearedSessionCookie().toString())
                .build());
    }
 
    private ResponseCookie buildSessionCookie(LoginResult result) {
        long maxAgeSeconds = Duration.between(LocalDateTime.now(), result.expiresAt()).getSeconds();
        return ResponseCookie.from(cookieProps.name(), result.sessionToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(cookieProps.domain())
                .path("/")
                .maxAge(Math.max(maxAgeSeconds, 0))
                .build();
    }
 
    private ResponseCookie clearedSessionCookie() {
        return ResponseCookie.from(cookieProps.name(), "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .domain(cookieProps.domain())
                .path("/")
                .maxAge(0)
                .build();
    }
}

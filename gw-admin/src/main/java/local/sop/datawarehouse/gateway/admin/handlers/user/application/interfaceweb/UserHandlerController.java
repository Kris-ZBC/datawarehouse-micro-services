package local.sop.datawarehouse.gateway.admin.handlers.user.application.interfaceweb;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.UserHandlerDirectory;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.request.RegisterInstructorRequest;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.response.CreatedUserResponse;

import local.sop.common.libs.infrastructure.security.context.AllowRoles;
import local.sop.common.libs.sharedkernel.enums.UserRole;
 
// Class-level, not method-level: every handler under /api/v1/admin/users
// is TECHUSER/INSTRUCTOR-only by nature of being an admin endpoint, so
// the policy is declared once here rather than repeated per method (and
// a new method added later inherits it automatically instead of
// silently being wide open — see WebMvcConfig's javadoc on why that
// matters). bc-instructor's own role check (TECHUSER/INSTRUCTOR only,
// ConflictException if not) remains a backstop regardless — this is
// defense-in-depth so an unauthorized caller gets rejected at the edge
// rather than after a round trip through the saga.
@AllowRoles({UserRole.TECHUSER, UserRole.INSTRUCTOR})
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserHandlerController {
 
    private final UserHandlerDirectory directory;
 
    public UserHandlerController(UserHandlerDirectory directory) {
        this.directory = directory;
    }
 
    @PostMapping(path = "/instructors", produces = "application/json")
    public CompletableFuture<ResponseEntity<CreatedUserResponse>> registerInstructor(
            @Valid @RequestBody RegisterInstructorRequest request) {
 
        return directory.registerInstructor(request)
                .thenApply(response -> ResponseEntity
                        .created(URI.create("/api/v1/admin/users/instructors/" + response.id()))
                        .body(response));
    }
 
}
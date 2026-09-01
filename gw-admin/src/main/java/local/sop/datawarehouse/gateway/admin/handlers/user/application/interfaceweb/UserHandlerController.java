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

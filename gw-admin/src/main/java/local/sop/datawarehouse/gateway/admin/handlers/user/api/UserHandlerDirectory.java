package local.sop.datawarehouse.gateway.admin.handlers.user.api;

import java.util.concurrent.CompletableFuture;

import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.request.RegisterInstructorRequest;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.response.CreatedUserResponse;

public interface UserHandlerDirectory {
    CompletableFuture<CreatedUserResponse> registerInstructor(RegisterInstructorRequest request);
}

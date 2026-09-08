package local.sop.datawarehouse.gateway.admin.handlers.user.application.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import local.sop.datawarehouse.gateway.admin.handlers.user.api.UserHandlerDirectory;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.request.RegisterInstructorRequest;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.response.CreatedUserResponse;
import local.sop.datawarehouse.gateway.admin.handlers.user.application.infrastructure.registation.RegistrationSagaAdapter;
import local.sop.common.libs.sharedkernel.enums.UserRole;

@Service
public class UserHandlerService implements UserHandlerDirectory {
    private static final Logger log = LoggerFactory.getLogger(UserHandlerService.class);    
    private final RegistrationSagaAdapter registrationSagaAdapter;
    private final ExecutorService adminGatewayExecutor;
    private final long sagaCallTimeoutSeconds;
    
    public UserHandlerService(RegistrationSagaAdapter registrationSagaAdapter, 
        ExecutorService adminGatewayExecutor, @Value("${registration-saga.call-timeout-seconds:10}") long sagaCallTimeoutSecond) {
        this.registrationSagaAdapter = registrationSagaAdapter;
        this.adminGatewayExecutor = adminGatewayExecutor;
        this.sagaCallTimeoutSeconds = sagaCallTimeoutSecond;
    }

    public CompletableFuture<CreatedUserResponse> registerInstructor(RegisterInstructorRequest request) {
        return CompletableFuture.supplyAsync(() -> 
            registrationSagaAdapter.register(request, UserRole.INSTRUCTOR), adminGatewayExecutor)
            .orTimeout(sagaCallTimeoutSeconds, TimeUnit.SECONDS)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error occurred while registering instructor: {}", ex.getMessage(), ex);
                } else {
                    log.info("Successfully registered instructor with ID: {}", result.id());
                }
            });

    }


}

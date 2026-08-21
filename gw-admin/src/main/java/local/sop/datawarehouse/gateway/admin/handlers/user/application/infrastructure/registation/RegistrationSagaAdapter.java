package local.sop.datawarehouse.gateway.admin.handlers.user.application.infrastructure.registation;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.request.RegisterInstructorRequest;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.response.CreatedUserResponse;
import local.sop.datawarehouse.shared.enums.UserRole;

/**
 *      
 * RegistrationSagaAdapter
 * Calls the bc-sop-registration-saga service to register a new user.
 */

@Component
public class RegistrationSagaAdapter {
    private final RestClient registration;

    public RegistrationSagaAdapter(@Qualifier("registration") RestClient registration) {
        this.registration = registration;
    }

    public CreatedUserResponse register(RegisterInstructorRequest request, UserRole userRole) {
        switch (userRole) {
            case INSTRUCTOR -> {
                return registration.post()
                    .uri("/internal/saga/registrations/instructors")
                    .body(request)
                    .retrieve()
                    .body(CreatedUserResponse.class);
            }
            default -> throw new IllegalArgumentException("Unsupported user role: " + userRole);
            
        }

    }

}
package local.sop.datawarehouse.registration.saga.application.interfaceweb;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.registration.saga.application.api.RegistrationDirectory;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateApprenticeRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.CreateInstructorRegistrationCmd;
import local.sop.datawarehouse.registration.saga.application.api.dto.apprentice.CreatedApprenticeResponse;
import local.sop.datawarehouse.registration.saga.application.api.dto.instructor.CreatedInstructorResponse;

@RestController
@RequestMapping("/internal/saga/registrations")
public class RegistrationSagaController {
	
    private RegistrationDirectory directory;

    RegistrationSagaController(RegistrationDirectory directory) {
        this.directory = directory;
    }

    @PostMapping(path = "/apprentices", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<CreatedApprenticeResponse> registerApprentice(@Valid @RequestBody CreateApprenticeRegistrationCmd cmd) {
        CreatedApprenticeResponse response = directory.registerApprentice(cmd);
        URI location = URI.create("/internal/saga/registrations/apprentices/" + response.apprenticeId());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @PostMapping(path = "/instructors", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<CreatedInstructorResponse> registerInstructor(@Valid @RequestBody CreateInstructorRegistrationCmd cmd) {
        CreatedInstructorResponse response = directory.registerInstructor(cmd);
        URI location = URI.create("/internal/saga/registrations/instructors/" + response.id());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }
}

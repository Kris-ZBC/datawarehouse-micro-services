package local.sop.sopinfo.education.saga.application.interfaceweb;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import local.sop.sopinfo.education.saga.application.api.EducationSagaDirectory;
import local.sop.sopinfo.education.saga.application.api.dto.*;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

@RestController
@RequestMapping("/internal/saga/educations")
public class EducationSagaController {
    
    private EducationSagaDirectory directory;
    private final String baseUrl = "/internal/saga/educations";

    public EducationSagaController(EducationSagaDirectory directory) {
        this.directory = directory;
    }

    @PostMapping(path = "/create", produces = "application/json")
    public ResponseEntity<EducationResponse> createEducation(@Valid @RequestBody CreateEducationCmd cmd) {
        EducationResponse response = directory.createEducation(cmd);
        URI location = URI.create(baseUrl + "/" + response.id());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @PutMapping(path = "/{educationId}/name", produces = "application/json")
    public ResponseEntity<EducationResponse> updateEducationName(@Valid @RequestBody UpdateEducationNameCmd cmd, @PathVariable UUID educationId) {
        EducationResponse response = directory.updateEducationName(educationId, cmd);
        URI location = URI.create(baseUrl + "/" + response.id());
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/{educationId}/category", produces = "application/json")
    public ResponseEntity<EducationResponse> updateEducationCategory(@Valid @RequestBody UpdateEducationCategoryCmd cmd, @PathVariable UUID educationId) {
        EducationResponse response = directory.updateEducationCategory(educationId, cmd);
        URI location = URI.create(baseUrl + "/" + response.id());
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/{educationId}/activate", produces = "application/json")
    public ResponseEntity<EducationResponse> activateEducation(@Valid @RequestBody CreateAuditlog cmd, @PathVariable UUID educationId) {
        EducationResponse response = directory.activateEducation(educationId, cmd);
        URI location = URI.create(baseUrl + "/" + response.id());
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/{educationId}/deactivate", produces = "application/json")
    public ResponseEntity<EducationResponse> deactivateEducation(@Valid @RequestBody CreateAuditlog cmd, @PathVariable UUID educationId) {
        EducationResponse response = directory.deactivateEducation(educationId, cmd);
        URI location = URI.create(baseUrl + "/" + response.id());
        return ResponseEntity.ok().location(location).body(response);
    }

    @PostMapping(path = "/education/instructor/create", produces = "application/json")
    public ResponseEntity<EducationInstructorResponse> createEducationInstructor(@Valid @RequestBody CreateEducationInstructorCmd cmd) {
        EducationInstructorResponse response = directory.createEducationInstructor(cmd);
        URI location = URI.create(baseUrl + "/instructors/" + response.id().key1() + "/" + response.id().key2());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @PutMapping(path = "/education-ref/{educationId}/instructor-ref/{instructorId}/activate", produces = "application/json")
    public ResponseEntity<EducationInstructorResponse> activateEducationInstructor(@Valid @RequestBody CreateAuditlog cmd,
            @PathVariable UUID educationId, @PathVariable UUID instructorId) {
        CompositeKey id = new CompositeKey(educationId, instructorId);
        EducationInstructorResponse response = directory.activateEducationInstructor(id, cmd);
        URI location = URI.create(baseUrl + "/instructors/" + response.id().key1() + "/" + response.id().key2());
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/education-ref/{educationId}/instructor-ref/{instructorId}/deactivate", produces = "application/json")
    public ResponseEntity<EducationInstructorResponse> deactivateEducationInstructor(@Valid @RequestBody CreateAuditlog cmd,
            @PathVariable UUID educationId, @PathVariable UUID instructorId) {
        CompositeKey id = new CompositeKey(educationId, instructorId);
        EducationInstructorResponse response = directory.deactivateEducationInstructor(id, cmd);
        URI location = URI.create(baseUrl + "/instructors/" + response.id().key1() + "/" + response.id().key2());
        return ResponseEntity.ok().location(location).body(response);
    }

}

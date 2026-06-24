package local.sop.sopinfo.education.interfaceweb;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.education.application.api.EducationDirectory;
import local.sop.sopinfo.education.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.application.api.dto.EducationResponse;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateUpdate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

@RestController
@RequestMapping("/internal/educations")
public class EducationController {

    private final EducationDirectory directory;

    public EducationController(EducationDirectory directory) {
        this.directory = directory;
    }

    @PostMapping("/create")
    public ResponseEntity<EducationResponse> createEducation(
            @RequestBody CreateEducationCmd cmd) {
        return ResponseEntity.ok(directory.createEducation(cmd));
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<EducationResponse> updateEducationName(@PathVariable UUID id, @RequestBody String name) {
        return ResponseEntity.ok(directory.updateEducationName(id, name));
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<EducationResponse> updateEducationCategory(@PathVariable UUID id, @RequestBody String category) {
        return ResponseEntity.ok(directory.updateEducationCategory(id, category));
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<EducationResponse> findById(@PathVariable UUID id) {
        return directory.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<EducationResponse>> findAll() {
        return ResponseEntity.ok(directory.findAll());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<EducationResponse> activate(@PathVariable UUID id) {
        EducationResponse response = directory.activateEducation(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<EducationResponse> deactivate(@PathVariable UUID id) {
        EducationResponse response = directory.deactivateEducation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/compensate-create")
    public ResponseEntity<ResponseCompensated> compensateCreate(@Valid @PathVariable UUID id, 
            @Valid @RequestBody PayloadCompensateCreate cmd) {
        ResponseCompensated response = directory.compensateCreateEducation(id, cmd.clazz(), cmd.sagaState());
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();              
    }

    @PostMapping("/{id}/compensate-name")
    public ResponseEntity<ResponseCompensated> compensateName(@Valid @PathVariable UUID id,
            @Valid @RequestBody PayloadCompensateUpdate cmd) {
        ResponseCompensated response = directory.compensateUpdateEducationName(id, cmd.clazz(), cmd.sagaOutcome(), cmd.previousValue());
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/compensate-category")
    public ResponseEntity<ResponseCompensated> compensateCategory(@Valid @PathVariable UUID id,
            @Valid @RequestBody PayloadCompensateUpdate cmd) {
        ResponseCompensated response = directory.compensateUpdateEducationCategory(id, cmd.clazz(), cmd.sagaOutcome(), cmd.previousValue());
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/compensate-activate")
    public ResponseEntity<ResponseCompensated> compensateActivate(@Valid @PathVariable UUID id,
            @Valid @RequestBody PayloadCompensateUpdate cmd) {
        ResponseCompensated response = directory.compensateActivateEducation(id, cmd.clazz(), cmd.sagaOutcome());
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/compensate-deactivate")
    public ResponseEntity<ResponseCompensated> compensateDeactivate(@Valid @PathVariable UUID id,
            @Valid @RequestBody PayloadCompensateUpdate cmd) {
        ResponseCompensated response = directory.compensateDeactivateEducation(id, cmd.clazz(), cmd.sagaOutcome());
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }
}
    

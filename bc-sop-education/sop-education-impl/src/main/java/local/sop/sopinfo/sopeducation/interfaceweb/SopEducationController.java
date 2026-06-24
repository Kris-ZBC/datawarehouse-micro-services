package local.sop.sopinfo.sopeducation.interfaceweb;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.application.api.SopEducationDirectory;
import local.sop.sopinfo.sopeducation.application.api.dto.CreateSopEducationCmd;
import local.sop.sopinfo.sopeducation.application.api.dto.CreatedSopEducationResult;
import local.sop.sopinfo.sopeducation.application.api.dto.SopEducationResponse;
import local.sop.sopinfo.sopeducation.application.api.dto.ToggleActivateSopEducationCmd;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/internal/sop-educations")
public class SopEducationController {

    private final SopEducationDirectory directory;

    public SopEducationController(SopEducationDirectory directory) {
        this.directory = directory;
    }

@PostMapping(consumes = "application/json", produces = "application/json")
@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
public ResponseEntity<CreatedSopEducationResult> create(@Valid @RequestBody CreateSopEducationCmd cmd) {
    CreatedSopEducationResult result = directory.create(cmd);
    UUID[] keys = result.id().keys();
    URI location = URI.create("/internal/sop-educations/sop-ref/" + keys[0] + "/education-ref/" + keys[1]);
    return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
}

    @PutMapping(path = "/toggle-active", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SopEducationResponse> toggleActive(@Valid @RequestBody ToggleActivateSopEducationCmd cmd) {
        SopEducationResponse response = directory.toggleActive(cmd);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/sop-ref/{sopId}/education-ref/{educationId}", produces = "application/json")
    public ResponseEntity<SopEducationResponse> findById(@PathVariable UUID sopId, @PathVariable UUID educationId) {
        return directory.findById(new CompositeKey(sopId, educationId)).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping(path = "/by-sop/{sopId}", produces = "application/json")
    public ResponseEntity<List<SopEducationResponse>> findBySopId(@PathVariable UUID sopId) {
        return ResponseEntity.ok(directory.getBySopRef(sopId));
    }
    
    @GetMapping(path = "/by-education/{educationId}", produces = "application/json")
    public ResponseEntity<List<SopEducationResponse>> findByEducationId(@PathVariable UUID educationId) {
        return ResponseEntity.ok(directory.getByEducationRef(educationId));
    }
    

}

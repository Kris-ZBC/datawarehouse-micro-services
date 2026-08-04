package local.sop.sopinfo.sopinstructor.interfaceweb;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.sopinstructor.application.api.SopInstructorDirectory;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreateSopInstructorCmd;
import local.sop.sopinfo.sopinstructor.application.api.dto.CreatedSopInstructorResult;
import local.sop.sopinfo.sopinstructor.application.api.dto.SopInstructorResponse;
import local.sop.sopinfo.sopinstructor.application.api.dto.ToggleActivateSopInstructorCmd;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

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
@RequestMapping("/internal/sop-instructors")
public class SopInstructorController {

    private final SopInstructorDirectory directory;

    public SopInstructorController(SopInstructorDirectory directory) {
        this.directory = directory;
    }

@PostMapping(produces = "application/json")
@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
public ResponseEntity<CreatedSopInstructorResult> create(@Valid @RequestBody CreateSopInstructorCmd cmd) {
    CreatedSopInstructorResult result = directory.create(cmd);
    UUID[] keys = result.id().keys();
    URI location = URI.create(("/internal/sop-instructors/sop-ref/" + keys[0] + "/instructor-ref/" + keys[1]));
    return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
}

    @PutMapping(path = "/toggle-active", produces = "application/json")
    public ResponseEntity<SopInstructorResponse> toggleActive(@Valid @RequestBody ToggleActivateSopInstructorCmd cmd) {
        SopInstructorResponse response = directory.toggleActive(cmd);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/sop-ref/{sopId}/instructor-ref/{instructorId}", produces = "application/json")
    public ResponseEntity<SopInstructorResponse> findById(@PathVariable UUID sopId, @PathVariable UUID instructorId) {
        return directory.findById(new CompositeKey(sopId, instructorId)).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping(path = "/by-sop/{sopId}", produces = "application/json")
    public ResponseEntity<List<SopInstructorResponse>> findBySopId(@PathVariable UUID sopId) {
        return ResponseEntity.ok(directory.getBySopRef(sopId));
    }
    
    @GetMapping(path = "/by-instructor/{instructorId}", produces = "application/json")
    public ResponseEntity<List<SopInstructorResponse>> findByInstructorId(@PathVariable UUID instructorId) {
        return ResponseEntity.ok(directory.getByInstructorRef(instructorId));
    }
    

}

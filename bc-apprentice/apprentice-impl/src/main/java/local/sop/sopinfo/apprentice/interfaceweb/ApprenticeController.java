package local.sop.sopinfo.apprentice.interfaceweb;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.apprentice.application.api.ApprenticeDirectory;
import local.sop.sopinfo.apprentice.application.api.dto.ApprenticeResponse;
import local.sop.sopinfo.apprentice.application.api.dto.CreateApprenticeCmd;
import local.sop.sopinfo.apprentice.application.api.dto.CreatedApprenticeResponse;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@RestController
@RequestMapping("/internal/apprentices")
public class ApprenticeController {
    private final ApprenticeDirectory directory;

    public ApprenticeController(ApprenticeDirectory directory) {
        this.directory = directory;
    }

    @PostMapping
    public ResponseEntity<CreatedApprenticeResponse> createApprentice(@Valid @RequestBody CreateApprenticeCmd cmd) {
        CreatedApprenticeResponse response = directory.createApprentice(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprenticeResponse> findById(@PathVariable UUID id) {
    return directory.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-education-line/{id}")
    public ResponseEntity<List<ApprenticeResponse>> findByEducationLineId(@PathVariable UUID id) {
        List<ApprenticeResponse> response = directory.findByEducationLineId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ApprenticeResponse>> findAll() {
        List<ApprenticeResponse> response = directory.findAll();
        return ResponseEntity.ok(response);
    }
    
    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
    @PutMapping(path = "/{id}/compensate/create", produces = "application/json")
    public ResponseEntity<ResponseCompensated> compensate(@PathVariable UUID id, @Valid @RequestBody PayloadCompensateCreate payload) {
        ResponseCompensated result = directory.compensate(id, payload.clazz(), payload.sagaState());
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
    }
}

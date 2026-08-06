package local.sop.datawarehouse.educationline.saga.application.interfaceweb;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.educationline.saga.application.api.EducationLineSagaDirectory;
import local.sop.datawarehouse.educationline.saga.application.api.dto.*;

@RestController
@RequestMapping("/internal/saga/educationlines")
public class EducationLineSagaController {
    
    private EducationLineSagaDirectory educationLineDirectory;

    public EducationLineSagaController(EducationLineSagaDirectory educationLineDirectory) {
        this.educationLineDirectory = educationLineDirectory;
    }

    @PostMapping(path = "/create", produces = "application/json")
    public ResponseEntity<EducationLineResponse> createEducationLine(@Valid @RequestBody CreateEducationLineCmd cmd) {
        EducationLineResponse response = educationLineDirectory.createEducationLine(cmd);
        URI location = URI.create("/internal/saga/educationlines/" + response.id());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @PutMapping(path = "/{id}/name", produces = "application/json")
    public ResponseEntity<EducationLineResponse> updateEducationLineName(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEducationLineNameCmd cmd
    ) {
        EducationLineResponse response = educationLineDirectory.updateEducationLineName(id, cmd);
        URI location = URI.create("/internal/saga/educationlines/" + id);
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/{id}/duration", produces = "application/json")
    public ResponseEntity<EducationLineResponse> updateEducationLineDuration(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEducationLineDurationCmd cmd
    ) {
        EducationLineResponse response = educationLineDirectory.updateEducationLineDuration(id, cmd);
        URI location = URI.create("/internal/saga/educationlines/" + id);
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/{id}/deactivate", produces = "application/json")
    public ResponseEntity<EducationLineResponse> deactivateEducationLine(@PathVariable UUID id, @Valid @RequestBody CreateAuditlogCmd cmd) {
        EducationLineResponse response = educationLineDirectory.deactivateEducationLine(id, cmd);
        URI location = URI.create("/internal/saga/educationlines/" + id);
        return ResponseEntity.ok().location(location).body(response);
    }

    @PutMapping(path = "/{id}/activate", produces = "application/json")
    public ResponseEntity<EducationLineResponse> activateEducationLine(@PathVariable UUID id, @Valid @RequestBody CreateAuditlogCmd cmd) {
        EducationLineResponse response = educationLineDirectory.activateEducationLine(id, cmd);
        URI location = URI.create("/internal/saga/educationlines/" + id);
        return ResponseEntity.ok().location(location).body(response);
    }
}
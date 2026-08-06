package local.sop.datawarehouse.anonymize.saga.application.interfaceweb;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.anonymize.saga.application.api.AnonymizeSagaDirectory;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.AnonymizeResponse;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.CreateAnonymizeCmd;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/internal/saga/anonymizations")
public class AnonymizeSagaController {

    private final AnonymizeSagaDirectory anonymizationDirectory;

    public AnonymizeSagaController(AnonymizeSagaDirectory anonymizationDirectory) {
        this.anonymizationDirectory = anonymizationDirectory;
    }

    @PostMapping(path = "", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<AnonymizeResponse> create(@Valid @RequestBody CreateAnonymizeCmd cmd) {
        AnonymizeResponse response = anonymizationDirectory.create(cmd);
        URI location = URI.create("/internal/saga/anonymizations" + response.anonymizationId());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }
}
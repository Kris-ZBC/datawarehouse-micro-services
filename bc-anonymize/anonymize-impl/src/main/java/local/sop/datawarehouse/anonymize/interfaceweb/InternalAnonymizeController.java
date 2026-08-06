package local.sop.datawarehouse.anonymize.interfaceweb;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.anonymize.application.api.AnonymizeDirectory;
import local.sop.datawarehouse.anonymize.application.api.dto.AnonymizeResponse;
import local.sop.datawarehouse.anonymize.application.api.dto.CompensateAnonymizeStatementCmd;
import local.sop.datawarehouse.anonymize.application.api.dto.CreateAnonymizeCmd;
import local.sop.datawarehouse.anonymize.application.api.dto.FetchByIdQuery;
import local.sop.datawarehouse.anonymize.application.api.dto.FetchByParamsQuery;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/anonymizations")
public class InternalAnonymizeController {

    private final AnonymizeDirectory anonymizationDirectory;

    public InternalAnonymizeController(AnonymizeDirectory anonymizationDirectory) {
        this.anonymizationDirectory = anonymizationDirectory;
    }

    @PostMapping(path = "", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<AnonymizeResponse> create(@Valid @RequestBody CreateAnonymizeCmd cmd) {
        AnonymizeResponse response = anonymizationDirectory.create(cmd);
        URI location = URI.create("/internal/anonymizations" + response.anonymizationId());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @GetMapping(path="/anonymize", produces = "application/json")
    public ResponseEntity<AnonymizeResponse> fetchById(@Valid @RequestBody FetchByIdQuery query) {
        return anonymizationDirectory.findById(query).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(path="", produces = "application/json")
    public ResponseEntity<List<AnonymizeResponse>> fetchByParams(@Valid @RequestBody FetchByParamsQuery query) {
        List<AnonymizeResponse> response = anonymizationDirectory.findByParams(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }

    @PostMapping(path="/{id}/compensate/create", produces="application/json")
    public ResponseEntity<ResponseCompensated> compensate(@PathVariable UUID id, @Valid @RequestBody CompensateAnonymizeStatementCmd cmd) {
        ResponseCompensated response = anonymizationDirectory.compensate(id, cmd.clazz(), cmd.sagaState());
        return ResponseEntity.ok(response);
    }
}
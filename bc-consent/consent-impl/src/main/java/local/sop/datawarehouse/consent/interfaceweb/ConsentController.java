package local.sop.datawarehouse.consent.interfaceweb;

import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.consent.application.api.ConsentDirectory;
import local.sop.datawarehouse.consent.application.api.dto.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/internal/consents")
public class ConsentController {

    private final ConsentDirectory consentDirectory;

    public ConsentController(ConsentDirectory consentDirectory) {
        this.consentDirectory = consentDirectory;
    }


    @PostMapping(path = "/statements", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<ConsentStatementResponse> create(@Valid @RequestBody CreateConsentStatementCmd cmd) {
        ConsentStatementResponse response = consentDirectory.createConsentStatement(cmd);
        URI location = URI.create("/internal/consents/statements" + response.consentStatementId());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }
    

    @PostMapping(path = "/consent/grant", produces = "application/json")
    public ResponseEntity<ConsentResponse> grantConsent(@Valid @RequestBody GrantConsentCmd cmd) {
        ConsentResponse response = consentDirectory.grantConsent(cmd);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/consent/{id}", produces = "application/json")
    public ResponseEntity<ConsentResponse> getConsent(@PathVariable("id") UUID id) {
        return consentDirectory.getConsent(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(path = "/consent", produces = "application/json")
    public ResponseEntity<ConsentResponse> getConsentForPersonAndPurpose(@Valid @RequestBody FetchConsentForPersonAndPurposeQuery query) {
        return consentDirectory.getConsentForPersonAndPurpose(query).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }


    @PostMapping(path = "/consent/withdraw", produces = "application/json")
    public ResponseEntity<ConsentResponse> withdrawConsent(@Valid @RequestBody RevokeConsentCmd cmd) {
        ConsentResponse response = consentDirectory.withdrawConsent(cmd);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/statements/statement", produces = "application/json")
    public ResponseEntity<ConsentStatementResponse> updateConsentStatement(@Valid @RequestBody UpdateConsentStatementCmd cmd) {
        ConsentStatementResponse response = consentDirectory.updateConsentStatement(cmd);
        return ResponseEntity.ok(response);
    }

       @GetMapping(path = "/statements", produces = "application/json")
    public ResponseEntity<List<ConsentStatementResponse>> getAllConsentStatements() {
        List<ConsentStatementResponse> statements = consentDirectory.getAllConsentStatements();
        return ResponseEntity.ok(statements);
    }

    @GetMapping("/statements/statement")
    public ResponseEntity<ConsentStatementResponse> getConsentStatement(@RequestParam UUID id) {
        return consentDirectory.getConsentStatement(new FetchConsentStatementQuery(id)).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
    @PostMapping(path ="/statements/{id}/compensate/create", produces = "application/json")
    public ResponseEntity<ResponseCompensated> compensate(@PathVariable UUID id, @Valid @RequestBody CompensateConsentStatementCmd cmd) {
        var result = consentDirectory.compensate(id, cmd.clazz(), cmd.sagaState());
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
    }

    @PutMapping(path ="consent/{id}/compensate/create", produces = "application/json")
    public ResponseEntity<ResponseCompensated> compensateConsent(@PathVariable UUID id, @Valid @RequestBody PayloadCompensateCreate payload) {
        ResponseCompensated result = consentDirectory.compensateConsent(id, payload.clazz(), payload.sagaState());
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
    }
    @PostMapping(path = "/consent/{id}/compensate/update", produces = "application/json")
    public ResponseEntity<ResponseCompensated> compensateConsentUpdate(@PathVariable UUID id, @Valid @RequestBody PayloadCompensateCreate payload) {
        ResponseCompensated result = consentDirectory.compensateConsentWithdrawalUpdate(
            id,
            payload.clazz(),
            payload.sagaState()
        );
        return ResponseEntity.ok(result);
}
}
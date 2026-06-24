package local.sop.sopinfo.consent.saga.application.interfaceweb;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.consent.saga.application.api.ConsentSagaDirectory;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.ConsentStatementResponse;
import local.sop.sopinfo.consent.saga.application.api.dto.CreateConsentStatementCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.GrantConsentCmd;
import local.sop.sopinfo.consent.saga.application.api.dto.RevokeConsentCmd;

@RestController
@RequestMapping("/internal/saga/consents")
public class ConsentSagaController {

    private ConsentSagaDirectory consentDirectory;

    ConsentSagaController(ConsentSagaDirectory consentDirectory) {
        this.consentDirectory = consentDirectory;
    }


    @PostMapping(path = "/statements", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<ConsentStatementResponse> create(@Valid @RequestBody CreateConsentStatementCmd cmd) {
        ConsentStatementResponse response = consentDirectory.createConsentStatement(cmd);
        URI location = URI.create("/internal/saga/consents/statements/" + response.consentStatementId());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @PostMapping(path = "/consent/grant", produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<ConsentResponse> grant(@Valid @RequestBody GrantConsentCmd cmd) {
        ConsentResponse response = consentDirectory.grant(cmd);
        URI location = URI.create("/internal/saga/consents/consent/grant/" + response.consentId());
        return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
    }

    @PostMapping(path = "/consent/withdraw", produces = "application/json")
    public ResponseEntity<ConsentResponse> withdraw(@Valid @RequestBody RevokeConsentCmd cmd) {
        ConsentResponse response = consentDirectory.withdraw(cmd);
        return ResponseEntity.ok(response);
    }
}

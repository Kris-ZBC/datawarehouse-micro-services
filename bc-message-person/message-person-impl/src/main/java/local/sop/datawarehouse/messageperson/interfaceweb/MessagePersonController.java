package local.sop.datawarehouse.messageperson.interfaceweb;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.messageperson.application.api.MessagePersonDirectory;
import local.sop.datawarehouse.messageperson.application.api.dto.CreateMessagePersonCmd;
import local.sop.datawarehouse.messageperson.application.api.dto.CreatedMessagePersonResult;
import local.sop.datawarehouse.messageperson.application.api.dto.MessagePersonResponse;
import local.sop.datawarehouse.messageperson.application.api.dto.ToggleActivateMessagePersonCmd;

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
@RequestMapping("/internal/message-persons")
public class MessagePersonController {

    private final MessagePersonDirectory directory;

    public MessagePersonController(MessagePersonDirectory directory) {
        this.directory = directory;
    }

@PostMapping(produces = "application/json")
@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
public ResponseEntity<CreatedMessagePersonResult> create(@Valid @RequestBody CreateMessagePersonCmd cmd) {
    CreatedMessagePersonResult result = directory.create(cmd);
    UUID[] keys = result.id().keys();
    URI location = URI.create(("/internal/message-persons/message-ref/" + keys[0] + "/person-ref/" + keys[1]));
    return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
}

    @PutMapping(path = "/toggle-active", produces = "application/json")
    public ResponseEntity<MessagePersonResponse> toggleActive(@Valid @RequestBody ToggleActivateMessagePersonCmd cmd) {
        MessagePersonResponse response = directory.toggleActive(cmd);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/message-ref/{messageId}/person-ref/{personId}", produces = "application/json")
    public ResponseEntity<MessagePersonResponse> findById(@PathVariable UUID messageId, @PathVariable UUID personId) {
        return directory.findById(new CompositeKey(messageId, personId)).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping(path = "/by-message/{messageId}", produces = "application/json")
    public ResponseEntity<List<MessagePersonResponse>> findByMessageId(@PathVariable UUID messageId) {
        return ResponseEntity.ok(directory.getByMessageRef(messageId));
    }
    
    @GetMapping(path = "/by-person/{personId}", produces = "application/json")
    public ResponseEntity<List<MessagePersonResponse>> findByPersonId(@PathVariable UUID personId) {
        return ResponseEntity.ok(directory.getByPersonRef(personId));
    }
    

}

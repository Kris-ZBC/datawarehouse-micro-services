package local.sop.datawarehouse.message.interfaceweb;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.application.api.MessageDirectory;
import local.sop.datawarehouse.message.application.api.dto.CreateMessageCmd;
import local.sop.datawarehouse.message.application.api.dto.MessageResponse;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/internal/messages")
public class InternalMessageController {

    private final MessageDirectory messageDirectory;

    public InternalMessageController(MessageDirectory messageDirectory) {
        this.messageDirectory = messageDirectory;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody CreateMessageCmd cmd) {
        return messageDirectory.create(cmd);
    }

	@GetMapping(path="/{id}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MessageResponse> findById(@PathVariable UUID id) {
        return messageDirectory.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

	@PutMapping(path="/{id}/compensate/create", produces = "application/json")
	public ResponseEntity<ResponseCompensated> compensate(@PathVariable UUID id, @Valid @RequestBody PayloadCompensateCreate cmd) {
		var result = messageDirectory.compensate(id, cmd.clazz(), cmd.sagaState());
		return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
	}
}
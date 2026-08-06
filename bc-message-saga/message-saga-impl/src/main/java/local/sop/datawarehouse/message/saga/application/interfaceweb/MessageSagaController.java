package local.sop.datawarehouse.message.saga.application.interfaceweb;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.message.saga.application.api.MessageSagaDirectory;
import local.sop.datawarehouse.message.saga.application.api.dto.CreateMessageCmd;
import local.sop.datawarehouse.message.saga.application.api.dto.MessageResponse;

@RestController
@RequestMapping("/internal/saga/messages")
public class MessageSagaController {

	private MessageSagaDirectory messageSagaDirectory;

	public MessageSagaController(MessageSagaDirectory messageSagaDirectory) {
		this.messageSagaDirectory = messageSagaDirectory;
	}

	@PostMapping(path = "/create", produces = "application/json")
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody CreateMessageCmd cmd) {
		MessageResponse response = messageSagaDirectory.createMessage(cmd);
		URI location = URI.create("/internal/saga/messages/create/" + response.id());
		return ResponseEntity.created(Objects.requireNonNull(location)).body(response);
	}
}

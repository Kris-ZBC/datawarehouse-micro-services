package local.sop.sopinfo.notification.interfaceweb;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.notification.application.api.NotificationDirectory;
import local.sop.sopinfo.notification.application.api.dto.CreateNotificationCmd;
import local.sop.sopinfo.notification.application.api.dto.CreateNotificationResult;
import local.sop.sopinfo.notification.application.api.dto.NotificationResponse;


@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {
	private final NotificationDirectory notification;

	public InternalNotificationController(NotificationDirectory notification) {
		this.notification = notification;
	}

	@PostMapping(path="/create", produces="application/json")
	public ResponseEntity<CreateNotificationResult> createNotification(@Valid @RequestBody CreateNotificationCmd cmd) {
		CreateNotificationResult result = new CreateNotificationResult(notification.createNotification(cmd));
		URI location = URI.create("/internal/notifications/" + result.id());
		return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
	}

	@GetMapping(path="/{id}", produces = "application/json")
	public ResponseEntity<NotificationResponse> findById(@PathVariable UUID id) {
		return notification.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PutMapping("/{id}/seen")
	public ResponseEntity<Void> makeNotificationSeen(@PathVariable UUID id) {
		notification.makeNotificationSeen(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
		notification.deleteNotification(id);
		return ResponseEntity.noContent().build();
	}

	// ping endpoint
	@GetMapping("/ping")
	public ResponseEntity<String> ping() {
		return ResponseEntity.ok("pong");
	}

}

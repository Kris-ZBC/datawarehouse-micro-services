package local.sop.sopinfo.personnotification.interfaceweb;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.personnotification.application.api.PersonNotificationDirectory;
import local.sop.sopinfo.personnotification.application.api.dto.CreatePersonNotificationCmd;
import local.sop.sopinfo.personnotification.application.api.dto.CreatedPersonNotificationResult;
import local.sop.sopinfo.personnotification.application.api.dto.PersonNotificationResponse;
import local.sop.sopinfo.personnotification.application.api.dto.ToggleActivatePersonNotificationCmd;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

@RestController
@RequestMapping("/internal/person-notifications")
public class PersonNotificationController {

    private final PersonNotificationDirectory directory;

    public PersonNotificationController(PersonNotificationDirectory directory) {
        this.directory = directory;
    }


@PostMapping(produces = "application/json")
@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
public ResponseEntity<CreatedPersonNotificationResult> create(@Valid @RequestBody CreatePersonNotificationCmd cmd) {
    CreatedPersonNotificationResult result = directory.create(cmd);
    UUID[] keys = result.id().keys();
    URI location = URI.create(("/internal/person-notifications/person-ref/" + keys[0] + "/notification-ref/" + keys[1]));
    return ResponseEntity.created(Objects.requireNonNull(location)).body(result);    
}

    @PutMapping(path = "/toggle-active", produces = "application/json")
    public ResponseEntity<PersonNotificationResponse> toggleActive(@Valid @RequestBody ToggleActivatePersonNotificationCmd cmd) {
        PersonNotificationResponse response = directory.toggleActive(cmd);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/person-ref/{personId}/notification-ref/{notificationId}", produces = "application/json")
    public ResponseEntity<PersonNotificationResponse> findById(@PathVariable UUID personId, @PathVariable UUID notificationId) {
        return directory.findById(new CompositeKey(personId, notificationId)).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping(path = "/by-person/{personId}", produces = "application/json")
    public ResponseEntity<List<PersonNotificationResponse>> findByPersonId(@PathVariable UUID personId) {
        return ResponseEntity.ok(directory.getByPersonRef(personId));
    }
    
    @GetMapping(path = "/by-notification/{notificationId}", produces = "application/json")
    public ResponseEntity<List<PersonNotificationResponse>> findByNotificationId(@PathVariable UUID notificationId) {
        return ResponseEntity.ok(directory.getByNotificationRef(notificationId));
    }
}

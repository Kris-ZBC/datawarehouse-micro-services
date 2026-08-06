package local.sop.datawarehouse.person.interfaceweb;

import java.util.UUID;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.person.application.api.PersonDirectory;
import local.sop.datawarehouse.person.application.api.dto.AddPhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.CreatePersonCmd;
import local.sop.datawarehouse.person.application.api.dto.PersonResponse;
import local.sop.datawarehouse.person.application.api.dto.PhoneNumberResponse;
import local.sop.datawarehouse.person.application.api.dto.RemovePhoneNumberCmd;
import local.sop.datawarehouse.person.application.api.dto.UpdatePersonCmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/internal/persons")
public class InternalPersonController {

	private static final Logger log = LoggerFactory.getLogger(InternalPersonController.class);

	private final PersonDirectory personDirectory;

	public InternalPersonController(PersonDirectory personDirectory) {
		this.personDirectory = personDirectory;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UUID create(@Valid @RequestBody CreatePersonCmd cmd) {
		log.info("Create person request received for organizationRef={}", cmd.organizationRef());

		UUID personId = personDirectory.create(cmd);

		log.info("Person created successfully with personId={}", personId);
		return personId;
	}

	@GetMapping
	public List<PersonResponse> findAll() {
		return personDirectory.findAll();
	}

	@GetMapping(path="/{id}", produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<PersonResponse> findById(@PathVariable UUID id) {
		return personDirectory.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/search")
	public List<PersonResponse> searchByName(@RequestParam("name") String name) {
		return personDirectory.searchByName(name);
	}

	@PatchMapping("/{id}")
	public PersonResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePersonCmd cmd) {
		log.info("Update person request received for personId={}", id);

		PersonResponse response = personDirectory.update(id, cmd);

		log.info("Person updated successfully for personId={}", id);
		return response;
	}

	@PostMapping("/phone-number")
	@ResponseStatus(HttpStatus.CREATED)
	public PhoneNumberResponse addPhoneNumber(@Valid @RequestBody AddPhoneNumberCmd cmd) {
		log.info("Add phone number request received for personId={}", cmd.personId());

		PhoneNumberResponse response = personDirectory.addPhoneNumber(cmd);

		log.info("Phone number added successfully for personId={}", cmd.personId());
		return response;
	}

	@DeleteMapping("/phone-number")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removePhoneNumber(@Valid @RequestBody RemovePhoneNumberCmd cmd) {
		log.info("Remove phone number request received for personId={}, phoneNumberId={}", cmd.personId(), cmd.phoneNumberId());

		personDirectory.removePhoneNumber(cmd);

		log.info("Phone number removed successfully for personId={}, phoneNumberId={}", cmd.personId(), cmd.phoneNumberId());
	}
    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
	@PutMapping(path = "/{id}/compensate/create", produces = "application/json")
	public ResponseEntity<ResponseCompensated> compensate(@PathVariable UUID id, @Valid @RequestBody PayloadCompensateCreate payload) {

		ResponseCompensated result = personDirectory.compensate(id, payload.clazz(), payload.sagaState());
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
	}
}
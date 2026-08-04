package local.sop.sopinfo.educationinstructor.interfaceweb;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import local.sop.sopinfo.educationinstructor.application.api.EducationInstructorDirectory;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreateEducationInstructorCmd;
import local.sop.sopinfo.educationinstructor.application.api.dto.CreatedEducationInstructorResult;
import local.sop.sopinfo.educationinstructor.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.educationinstructor.application.api.dto.ToggleActivateEducationInstructorCmd;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;

@RestController
@RequestMapping("/internal/education-instructors")
public class EducationInstructorController {
	
	private final EducationInstructorDirectory directory;

	public EducationInstructorController(EducationInstructorDirectory directory) {
		this.directory = directory;
	}

	@PostMapping(produces = "application/json")
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	public ResponseEntity<CreatedEducationInstructorResult> create(@Valid @RequestBody CreateEducationInstructorCmd cmd) {
		CreatedEducationInstructorResult result = directory.create(cmd);
		UUID[] keys = result.id().keys();
		URI location = URI.create(("/internal/education-instructors/education-ref/" + keys[0] + "/instructor-ref/" + keys[1]));
		return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
	}

	@PutMapping(path = "/toggle-active", produces = "application/json")
    public ResponseEntity<EducationInstructorResponse> toggleActive(@Valid @RequestBody ToggleActivateEducationInstructorCmd cmd) {
        EducationInstructorResponse response = directory.toggleActive(cmd);
        return ResponseEntity.ok(response);
    }

	@GetMapping(produces = "application/json")
	public ResponseEntity<List<EducationInstructorResponse>> getAll() {
		return ResponseEntity.ok(directory.getAll());
	}

	@GetMapping(path = "/education-ref/{educationId}", produces = "application/json")
	public ResponseEntity<List<EducationInstructorResponse>> findByEducationRef(@PathVariable UUID educationId) {
		return ResponseEntity.ok(directory.getByEducationRef(educationId));
	}

	@GetMapping(path = "/instructor-ref/{instructorId}", produces = "application/json")
	public ResponseEntity<List<EducationInstructorResponse>> findByInstructorRef(@PathVariable UUID instructorId) {
		return ResponseEntity.ok(directory.getByInstructorRef(instructorId));
	}

	@GetMapping(path = "/education-ref/{educationId}/instructor-ref/{instructorId}", produces = "application/json")
	public ResponseEntity<EducationInstructorResponse> findById(@PathVariable UUID educationId,@PathVariable UUID instructorId) {
		return directory.findById(new CompositeKey(educationId, instructorId)).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	// Compensate endpoints

	@PostMapping("/education/{educationId}/instructor/{instructorId}/compensate-create")
	public ResponseEntity<ResponseCompensated> compensate(@Valid @PathVariable UUID educationId,
		@Valid @PathVariable UUID instructorId, @Valid @RequestBody PayloadCompensateCreate cmd)
	{
		ResponseCompensated response = directory.compensateCreateEducationInstructor(
				new CompositeKey(educationId, instructorId), cmd.clazz(), cmd.sagaState());
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}
	
	@PostMapping("/education/{educationId}/instructor/{instructorId}/compensate-activate")
	public ResponseEntity<ResponseCompensated> compensateActivate(@Valid @PathVariable UUID educationId,
		@Valid @PathVariable UUID instructorId, @Valid @RequestBody PayloadCompensateCreate cmd)
	{
		ResponseCompensated response = directory.compensateActivateEducationInstructor(
				new CompositeKey(educationId, instructorId), cmd.clazz(), cmd.sagaState());
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}

	@PostMapping("/education/{educationId}/instructor/{instructorId}/compensate-deactivate")
	public ResponseEntity<ResponseCompensated> compensateDeactivate(@Valid @PathVariable UUID educationId,
		@Valid @PathVariable UUID instructorId, @Valid @RequestBody PayloadCompensateCreate cmd)
	{
		ResponseCompensated response = directory.compensateDeactivateEducationInstructor(
				new CompositeKey(educationId, instructorId), cmd.clazz(), cmd.sagaState());
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}
}

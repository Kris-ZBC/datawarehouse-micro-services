package local.sop.sopinfo.educationline.interfaceweb;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

import local.sop.sopinfo.educationline.application.api.EducationLineDirectory;
import local.sop.sopinfo.educationline.application.api.dto.CreateEducationLineCmd;
import local.sop.sopinfo.educationline.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineDurationCmd;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineNameCmd;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;


@RestController
@RequestMapping("/internal/educationlines")
public class EducationLineController {
	private final EducationLineDirectory educationLineDirectory;

	public EducationLineController(EducationLineDirectory educationLineDirectory) {
		this.educationLineDirectory = educationLineDirectory;
	}
	
	@PostMapping("/create")
	public ResponseEntity<EducationLineResponse> createEducationLine(@Valid @RequestBody CreateEducationLineCmd request) {
		EducationLineResponse response = educationLineDirectory.createEducationLine(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	 
	@GetMapping("/getall")
	public ResponseEntity<List<EducationLineResponse>> findAll() {
		List<EducationLineResponse> response = educationLineDirectory.findAll();
		return ResponseEntity.ok(response);
	}	

	@GetMapping(path = "/{id}", produces = "application/json")
	public ResponseEntity<EducationLineResponse> findById(@Valid @PathVariable UUID id) {
		return educationLineDirectory.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}/education-ref")
	public ResponseEntity<List<EducationLineResponse>> findByEducationRef(@Valid @PathVariable UUID id) {
		List<EducationLineResponse> response = educationLineDirectory.findByEducationRef(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/name")
	public ResponseEntity<EducationLineResponse> updateName(@Valid @PathVariable UUID id, @RequestBody UpdateEducationLineNameCmd request) {
		EducationLineResponse response = educationLineDirectory.updateEducationLineName(id, request);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/duration")
	public ResponseEntity<EducationLineResponse> updateDuration(@Valid @PathVariable UUID id, @RequestBody UpdateEducationLineDurationCmd request) {
		EducationLineResponse response = educationLineDirectory.updateEducationLineDuration(id, request);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<EducationLineResponse> deactivate(@Valid @PathVariable UUID id) {
		EducationLineResponse response = educationLineDirectory.deactivateEducationLine(id);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}/activate")
	public ResponseEntity<EducationLineResponse> activate(@Valid @PathVariable UUID id) {
		EducationLineResponse response = educationLineDirectory.activateEducationLine(id);
		return ResponseEntity.ok(response);
	}
    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }

	@PostMapping("/{id}/compensate")
	public ResponseEntity<ResponseCompensated> compensate(@Valid @PathVariable UUID id,
			@Valid @RequestBody PayloadCompensateCreate cmd) {
		ResponseCompensated response = educationLineDirectory.compensate(id, cmd.clazz(), cmd.sagaState());
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{id}/compensateactivate")
	public ResponseEntity<ResponseCompensated> compensateActivate(@Valid @PathVariable UUID id,
			@Valid @RequestBody PayloadCompensateCreate cmd) {
		ResponseCompensated response = educationLineDirectory.compensate(id, cmd.clazz(), cmd.sagaState());
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{id}/compensatedeactivate")
	public ResponseEntity<ResponseCompensated> compensateDeactivate(@Valid @PathVariable UUID id,
			@Valid @RequestBody PayloadCompensateCreate cmd) {
		ResponseCompensated response = educationLineDirectory.compensate(id, cmd.clazz(), cmd.sagaState());
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/compensatename")
	public ResponseEntity<ResponseCompensated> compensateName(@Valid @PathVariable UUID id,
			@Valid @RequestBody PayloadCompensateCreate cmd, @RequestBody UpdateEducationLineNameCmd nameReq) {
		ResponseCompensated response = educationLineDirectory.compensateName(id, cmd.clazz(), cmd.sagaState(), nameReq);
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/compensateduration")
	public ResponseEntity<ResponseCompensated> compensateDuration(@Valid @PathVariable UUID id,
			@Valid @RequestBody PayloadCompensateCreate cmd, @RequestBody UpdateEducationLineDurationCmd durationReq) {
		ResponseCompensated response = educationLineDirectory.compensateDuration(id, cmd.clazz(), cmd.sagaState(), durationReq);
		return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
	}
}

package local.sop.sopinfo.auditlog.interfaceweb;

import jakarta.validation.Valid;
import local.sop.sopinfo.auditlog.application.api.AuditlogDirectory;
import local.sop.sopinfo.auditlog.application.api.dto.AuditlogResponse;
import local.sop.sopinfo.auditlog.application.api.dto.CompensateAuditlogCmd;
import local.sop.sopinfo.auditlog.application.api.dto.CreateAuditlogCmd;
import local.sop.sopinfo.auditlog.application.api.dto.CreatedAuditlogResponse;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/internal/auditlogs")
public class AuditlogController {
	private final AuditlogDirectory directory;
	private static final Logger log = LoggerFactory.getLogger(AuditlogController.class);

	public AuditlogController(AuditlogDirectory directory) {
		this.directory = directory;
	}

	@PostMapping
	public ResponseEntity<CreatedAuditlogResponse> createAuditlog(@Valid @RequestBody CreateAuditlogCmd cmd) {
		log.info(
				"Received create audit log request: actorRef={}, actorType={}, severity={}, originSystem={}, originService={}, originComponent={}, data={}, description={}",
				cmd.actorRef(),
				cmd.actorType(),
				cmd.severity(),
				cmd.originSystem(),
				cmd.originService(),
				cmd.originComponent(),
				cmd.data(),
				cmd.description());
		CreatedAuditlogResponse response = directory.createAuditlog(cmd);
		log.info("Audit log created successfully with id={}", response.id());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<AuditlogResponse>> findAll() {
		log.info("Received request to fetch all audit logs");
		List<AuditlogResponse> response = directory.findAll();
		log.info("Fetched {} audit logs", response.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/search")
	public ResponseEntity<List<AuditlogResponse>> search(
			@RequestParam(required = false) UUID id,
			@RequestParam(required = false) UUID actorRef,
			@RequestParam(required = false) ActorType actorType,
			@RequestParam(required = false) Severity severity,
			@RequestParam(required = false) String originSystem,
			@RequestParam(required = false) String originService,
			@RequestParam(required = false) String originComponent) {

		log.info( "Received audit log search request: id={}, actorRef={}, actorType={}, severity={}, originSystem={}, originService={}, originComponent={}",
			id, actorRef, actorType, severity, originSystem, originService, originComponent);

		List<AuditlogResponse> response = directory.findBySearchParams(id, actorRef, actorType, severity, originSystem,
				originService, originComponent);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuditlogResponse> getById(@PathVariable UUID id) {
		return directory.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}/compensate/create")
	public ResponseEntity<ResponseCompensated> compensate(
		@PathVariable UUID id,
		@Valid @RequestBody CompensateAuditlogCmd cmd) {
		var result = directory.compensate(id, cmd.clazz(), cmd.sagaState());
		return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
	}
	
    

    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}

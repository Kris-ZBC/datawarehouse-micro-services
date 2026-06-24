package local.sop.sopinfo.instructor.interfaceweb;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.instructor.application.api.InstructorDirectory;
import local.sop.sopinfo.instructor.application.api.dto.CreateInstructorCmd;
import local.sop.sopinfo.instructor.application.api.dto.CreatedInstructorResponse;
import local.sop.sopinfo.instructor.application.api.dto.InstructorResponse;
import local.sop.sopinfo.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.sopinfo.sharedkernel.sagas.compensate.response.ResponseCompensated;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController

@RequestMapping("/internal/instructors")

public class InternalInstructorController {

    private static final Logger log = LoggerFactory.getLogger(InternalInstructorController.class);
    
    private final InstructorDirectory instructorDirectory;
    
    public InternalInstructorController(InstructorDirectory instructorDirectory) {
        this.instructorDirectory = instructorDirectory;
    }

    @GetMapping
    public List<InstructorResponse> getAllInstructors() {
        log.info("Received request to fetch all instructors");
        List<InstructorResponse> result = instructorDirectory.findAll();
        log.info("Returning {} instructors", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public InstructorResponse findInstructorId(@PathVariable UUID id) {
        log.info("Received request to fetch instructor with id={}", id);
        InstructorResponse response = instructorDirectory.findById(id);
        log.info("Returning instructor with id={}", id);
        return response;
    }

    @PostMapping
    public CreatedInstructorResponse createInstructor(@Valid @RequestBody CreateInstructorCmd cmd) {
        log.info("Received request to create instructor with personRef={}", cmd.personRef());
        CreatedInstructorResponse response = instructorDirectory.createInstructor(cmd);
        log.info("Instructor created with id={}", response.id());
        return response;
    }
    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
    @PutMapping(path = "/{id}/compensate/create", produces = "application/json")
    public ResponseEntity<ResponseCompensated> compensate(@PathVariable UUID id, @Valid @RequestBody PayloadCompensateCreate payload) {
        ResponseCompensated result = instructorDirectory.compensate(id, payload.clazz(), payload.sagaState());
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
    }
}

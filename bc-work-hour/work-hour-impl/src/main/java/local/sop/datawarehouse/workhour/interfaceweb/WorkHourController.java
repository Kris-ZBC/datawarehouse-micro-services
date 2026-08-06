package local.sop.datawarehouse.workhour.interfaceweb;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.datawarehouse.workhour.application.api.WorkHourDirectory;
import local.sop.datawarehouse.workhour.application.api.dto.CreateWorkHourCmd;
import local.sop.datawarehouse.workhour.application.api.dto.CreatedWorkHourResult;
import local.sop.datawarehouse.workhour.application.api.dto.UpdateWorkHourCmd;
import local.sop.datawarehouse.workhour.application.api.dto.WorkHourResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/internal/workhours")
public class WorkHourController {

    private final WorkHourDirectory directory;

    public WorkHourController(WorkHourDirectory directory) {
        this.directory = directory;
    }

    @PostMapping(produces = "application/json")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ResponseEntity<CreatedWorkHourResult> create(@Valid @RequestBody CreateWorkHourCmd cmd) {

        CreatedWorkHourResult result = directory.create(cmd);

        URI location = URI.create("/internal/workhours/" + result.id());

        return ResponseEntity.created(Objects.requireNonNull(location))
            .body(result);
    }

    @PutMapping(produces = "application/json")
    public ResponseEntity<WorkHourResponse> update(
            @Valid @RequestBody UpdateWorkHourCmd cmd) {

        WorkHourResponse response = directory.update(cmd);

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<WorkHourResponse> findById(
            @PathVariable UUID id) {

        return directory.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<WorkHourResponse>> findAll() {
        return ResponseEntity.ok(directory.getAll());
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}

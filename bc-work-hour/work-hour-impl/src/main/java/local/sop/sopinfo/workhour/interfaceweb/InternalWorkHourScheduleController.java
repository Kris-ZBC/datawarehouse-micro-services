package local.sop.sopinfo.workhour.interfaceweb;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.workhour.application.api.WorkHourDirectory;
import local.sop.sopinfo.workhour.application.api.dto.CreateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.application.api.dto.CreatedWorkHourScheduleResult;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleIdQuery;
import local.sop.sopinfo.workhour.application.api.dto.FindByScheduleParamsQuery;
import local.sop.sopinfo.workhour.application.api.dto.UpdateWorkHourScheduleCmd;
import local.sop.sopinfo.workhour.application.api.dto.WorkScheduleResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/internal/workhours")
public class InternalWorkHourScheduleController {

    private final WorkHourDirectory workHours;

    InternalWorkHourScheduleController(WorkHourDirectory workHours) {
        this.workHours = workHours;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreatedWorkHourScheduleResult> create(@Valid @RequestBody CreateWorkHourScheduleCmd cmd) {
       CreatedWorkHourScheduleResult result = new CreatedWorkHourScheduleResult(workHours.create(cmd));
       URI location = URI.create("/internal/workhours/" +result.id());
       return ResponseEntity.created(Objects.requireNonNull(location)).body(result);
    }

    @GetMapping(path = "/workhour", produces = "application/json")
    public ResponseEntity<WorkScheduleResponse> readById(@Valid @RequestParam UUID id) {
        FindByScheduleIdQuery query = new FindByScheduleIdQuery(id);
        return workHours.readById(query).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<WorkScheduleResponse>> readBySearchParams(
        @Valid @RequestParam(required=false) UUID id,
        @Valid @RequestParam(required = false) String startDate,
        @Valid @RequestParam(required = false) String endDate,
        @Valid @RequestParam(required = false) String day,
        @Valid @RequestParam(required=false) UUID sopRef
        
    ) {
        FindByScheduleParamsQuery query = new FindByScheduleParamsQuery(id, startDate, endDate, day, sopRef);
        return ResponseEntity.ok(workHours.readByParams(query));
    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateWorkHourScheduleCmd cmd) {
        workHours.update(cmd);
        return ResponseEntity.noContent().build(); 
    }   

    @DeleteMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> delete(@Valid @RequestBody FindByScheduleIdQuery query) {
        workHours.delete(query);
        return ResponseEntity.noContent().build(); 
    }

    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}

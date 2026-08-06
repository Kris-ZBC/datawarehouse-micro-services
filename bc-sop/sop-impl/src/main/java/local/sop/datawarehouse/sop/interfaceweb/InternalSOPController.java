package local.sop.datawarehouse.sop.interfaceweb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import local.sop.datawarehouse.sop.application.api.SOPDirectory;
import local.sop.datawarehouse.sop.application.api.dto.SOPQuery;
import local.sop.datawarehouse.sop.application.api.dto.SopResponse;

import java.util.UUID;

@RestController
@RequestMapping("internal/sop")
public class InternalSOPController {
    private final SOPDirectory directory;
    
    public InternalSOPController(SOPDirectory directory) {
        this.directory = directory;
    }
    
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<SopResponse> findById(@PathVariable UUID id) {
        return directory.findById(new SOPQuery(id)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}
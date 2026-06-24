package local.sop.sopinfo.sop.interfaceweb;

import local.sop.sopinfo.sop.application.api.SOPDirectory;
import local.sop.sopinfo.sop.application.api.dto.SOPQuery;
import local.sop.sopinfo.sop.application.api.dto.SopResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
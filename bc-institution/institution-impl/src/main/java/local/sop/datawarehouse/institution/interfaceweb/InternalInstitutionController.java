package local.sop.datawarehouse.institution.interfaceweb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import local.sop.datawarehouse.institution.application.api.InstitutionDirectory;
import local.sop.datawarehouse.institution.application.api.dto.InstitutionQuery;
import local.sop.datawarehouse.institution.application.api.dto.InstitutionResponse;

import java.util.UUID;

@RestController
@RequestMapping("/internal/institutions")
public class InternalInstitutionController {

    private final InstitutionDirectory directory;

    public InternalInstitutionController(InstitutionDirectory directory) {
        this.directory = directory;
    }

    @GetMapping("/{id}")
    public InstitutionResponse findById(@PathVariable UUID id) {
        return directory.findById(new InstitutionQuery(id));
    }

    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}

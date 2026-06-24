package local.sop.sopinfo.organisation.interfaceweb;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import local.sop.sopinfo.organisation.application.api.OrganisationDirectory;

@RestController
@RequestMapping("/internal/organisations")
public class InternalOrganisationController{
    OrganisationDirectory directory;
    public InternalOrganisationController(OrganisationDirectory directory){
        this.directory = directory;
    }

    @GetMapping
    public ResponseEntity<?> findById(@Valid @RequestParam UUID id){
        return directory.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}
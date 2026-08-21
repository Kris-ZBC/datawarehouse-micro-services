package local.sop.datawarehouse.organisation.interfaceweb;

import java.util.List;
import java.util.UUID;

import local.sop.datawarehouse.organisation.application.api.OrganisationDirectory;
import local.sop.datawarehouse.organisation.application.api.dto.OrganisationResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/organisations")
public class InternalOrganisationController{
    OrganisationDirectory directory;
    public InternalOrganisationController(OrganisationDirectory directory){
        this.directory = directory;
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<OrganisationResponse> findById(@PathVariable UUID id){
        return directory.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<OrganisationResponse>> findAll(){
        return ResponseEntity.ok(directory.findAll());
    }

    // Ping endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("PONG!");
    }
}
package local.sop.datawarehouse.gateway.common.handlers.organisation.application.interfaceweb;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import local.sop.datawarehouse.gateway.common.handlers.organisation.api.OrganisationDirectory;
import local.sop.datawarehouse.gateway.common.handlers.organisation.api.dto.response.OrganisationResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/common/organisations")
public class OrganisationController {

    private final OrganisationDirectory organisationDirectory;

    public OrganisationController(OrganisationDirectory organisationDirectory) {
        this.organisationDirectory = organisationDirectory;
    }

    @GetMapping(produces = "application/json")
    public CompletableFuture<List<OrganisationResponse>> getAll() {
        return CompletableFuture.supplyAsync(() -> organisationDirectory.getAllOrganisations());
    }
    


}

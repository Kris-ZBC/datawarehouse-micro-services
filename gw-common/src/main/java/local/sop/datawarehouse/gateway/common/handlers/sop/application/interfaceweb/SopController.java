package local.sop.datawarehouse.gateway.common.handlers.sop.application.interfaceweb;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import local.sop.datawarehouse.gateway.common.handlers.sop.api.SopDirectory;
import local.sop.datawarehouse.gateway.common.handlers.sop.api.dto.response.SopResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/common/sops")
public class SopController {
    private final SopDirectory sopDirectory;

    public SopController(SopDirectory sopDirectory) {
        this.sopDirectory = sopDirectory;
    }

    @GetMapping(produces = "application/json")
    public CompletableFuture<List<SopResponse>> getAll() {
        return CompletableFuture.supplyAsync(() -> sopDirectory.getAllSops());
    }
    

}

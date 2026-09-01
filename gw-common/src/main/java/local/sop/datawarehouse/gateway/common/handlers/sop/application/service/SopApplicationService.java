package local.sop.datawarehouse.gateway.common.handlers.sop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import local.sop.datawarehouse.gateway.common.handlers.sop.api.SopDirectory;
import local.sop.datawarehouse.gateway.common.handlers.sop.api.dto.response.SopResponse;
import local.sop.datawarehouse.gateway.common.handlers.sop.application.ports.SopPort;

@Service
public class SopApplicationService implements SopDirectory {

    private final SopPort sopPort;

    public SopApplicationService(SopPort sopPort) {
        this.sopPort = sopPort;
    }

    @Override
    public List<SopResponse> getAllSops() {
        return sopPort.getAllSops();
    }
    

}

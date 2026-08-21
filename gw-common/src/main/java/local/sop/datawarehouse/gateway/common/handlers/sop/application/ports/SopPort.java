package local.sop.datawarehouse.gateway.common.handlers.sop.application.ports;

import java.util.List;

import local.sop.datawarehouse.gateway.common.handlers.sop.api.dto.response.SopResponse;

public interface SopPort {
    List<SopResponse> getAllSops();
}

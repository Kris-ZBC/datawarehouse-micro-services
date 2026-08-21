package local.sop.datawarehouse.gateway.common.handlers.sop.api;

import java.util.List;

import local.sop.datawarehouse.gateway.common.handlers.sop.api.dto.response.SopResponse;

public interface SopDirectory {
    List<SopResponse> getAllSops();
}

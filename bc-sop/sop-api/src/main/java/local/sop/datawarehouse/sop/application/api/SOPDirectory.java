package local.sop.datawarehouse.sop.application.api;


import java.util.List;
import java.util.Optional;

import local.sop.datawarehouse.sop.application.api.dto.SOPQuery;
import local.sop.datawarehouse.sop.application.api.dto.SopResponse;

public interface SOPDirectory {
    public Optional<SopResponse> findById(SOPQuery query);
    public List<SopResponse> findAll();
}

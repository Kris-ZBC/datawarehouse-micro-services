package local.sop.sopinfo.sop.application.api;


import java.util.Optional;

import local.sop.sopinfo.sop.application.api.dto.SOPQuery;
import local.sop.sopinfo.sop.application.api.dto.SopResponse;

public interface SOPDirectory {
    public Optional<SopResponse> findById(SOPQuery query);
}

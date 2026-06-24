package local.sop.sopinfo.institution.application.api;


import local.sop.sopinfo.institution.application.api.dto.InstitutionQuery;
import local.sop.sopinfo.institution.application.api.dto.InstitutionResponse;

public interface InstitutionDirectory {
    InstitutionResponse findById(InstitutionQuery query);
}

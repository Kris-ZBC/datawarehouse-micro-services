package local.sop.datawarehouse.institution.application.api;


import local.sop.datawarehouse.institution.application.api.dto.InstitutionQuery;
import local.sop.datawarehouse.institution.application.api.dto.InstitutionResponse;

public interface InstitutionDirectory {
    InstitutionResponse findById(InstitutionQuery query);
}

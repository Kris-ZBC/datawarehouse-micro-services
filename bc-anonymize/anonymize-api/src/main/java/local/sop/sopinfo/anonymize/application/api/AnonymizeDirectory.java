package local.sop.sopinfo.anonymize.application.api;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.anonymize.application.api.dto.FetchByParamsQuery;
import local.sop.sopinfo.anonymize.application.api.dto.AnonymizeResponse;
import local.sop.sopinfo.anonymize.application.api.dto.CreateAnonymizeCmd;
import local.sop.sopinfo.anonymize.application.api.dto.FetchByIdQuery;
import local.sop.common.libs.sharedkernel.sagas.compensate.Compensatable;

public interface AnonymizeDirectory extends Compensatable {

    AnonymizeResponse create(CreateAnonymizeCmd cmd);

    Optional<AnonymizeResponse> findById(FetchByIdQuery query);

    List<AnonymizeResponse> findByParams(FetchByParamsQuery query);
}
package local.sop.sopinfo.anonymize.saga.application.api;

import local.sop.sopinfo.anonymize.saga.application.api.dto.CreateAnonymizeCmd;
import local.sop.sopinfo.anonymize.saga.application.api.dto.AnonymizeResponse;

public interface AnonymizeSagaDirectory {
    AnonymizeResponse create(CreateAnonymizeCmd cmd);
}
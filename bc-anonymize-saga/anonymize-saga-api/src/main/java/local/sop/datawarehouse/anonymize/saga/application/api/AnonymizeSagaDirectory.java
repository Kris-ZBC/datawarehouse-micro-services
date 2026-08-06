package local.sop.datawarehouse.anonymize.saga.application.api;

import local.sop.datawarehouse.anonymize.saga.application.api.dto.AnonymizeResponse;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.CreateAnonymizeCmd;

public interface AnonymizeSagaDirectory {
    AnonymizeResponse create(CreateAnonymizeCmd cmd);
}
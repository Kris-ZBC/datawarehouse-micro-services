package local.sop.datawarehouse.anonymize.domain.service;

import local.sop.datawarehouse.anonymize.domain.model.Anonymize;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.PersonRef;

public interface AnonymizeDomain {

    Anonymize create(PersonRef personRef);
}
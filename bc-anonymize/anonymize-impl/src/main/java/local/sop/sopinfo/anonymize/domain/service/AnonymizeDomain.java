package local.sop.sopinfo.anonymize.domain.service;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;

public interface AnonymizeDomain {

    Anonymize create(PersonRef personRef);
}
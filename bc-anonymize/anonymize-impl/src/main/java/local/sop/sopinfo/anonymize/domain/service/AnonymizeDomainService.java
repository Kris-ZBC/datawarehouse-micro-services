package local.sop.sopinfo.anonymize.domain.service;

import org.springframework.stereotype.Service;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;

@Service
public class AnonymizeDomainService implements AnonymizeDomain {

    @Override
    public Anonymize create(PersonRef personRef) {
        return Anonymize.create(personRef);
    }
}
package local.sop.datawarehouse.anonymize.domain.model;

import local.sop.datawarehouse.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.datawarehouse.anonymize.domain.model.valueobjects.PersonRef;

public class Anonymize {

    private final AnonymizeId anonymizationId;
    private final PersonRef personRef;

    private Anonymize(AnonymizeId anonymizationId, PersonRef personRef) {
        this.anonymizationId = anonymizationId;
        this.personRef = personRef;
    }

    public static Anonymize create(PersonRef personRef) {
        return new Anonymize(AnonymizeId.newId(), personRef);
    }

    public static Anonymize of(AnonymizeId anonymizationId, PersonRef personRef) {
        return new Anonymize(anonymizationId, personRef);
    }

    public AnonymizeId getAnonymizationId() {
        return anonymizationId;
    }

    public PersonRef getPersonRef() {
        return personRef;
    }
}
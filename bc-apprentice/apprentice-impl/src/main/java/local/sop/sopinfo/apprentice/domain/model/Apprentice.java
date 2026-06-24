package local.sop.sopinfo.apprentice.domain.model;

import java.util.Map;
import java.util.StringJoiner;

import local.sop.sopinfo.apprentice.domain.model.valueobjects.*;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

public class Apprentice {
    private final DomainId apprenticeId;
    private final DomainId personRef;
    private final DomainId educationLineRef;

    // internal constructor used by factory methods / builder
    private Apprentice(
            DomainId apprenticeId,
            DomainId personRef,
            DomainId educationLineRef) {
        this.apprenticeId = apprenticeId;
        this.personRef = personRef;
        this.educationLineRef = educationLineRef;
    }

    public DomainId getApprenticeId() {return apprenticeId;}
    public DomainId getPersonRef() {return personRef;}
    public DomainId getEducationLineRef() {return educationLineRef;}

    public Apprentice withApprenticeId(DomainId newId) {
        return new Apprentice(newId, this.personRef, this.educationLineRef);
    }
    public Apprentice withPersonRef(DomainId newPersonRef) {
        return new Apprentice(this.apprenticeId, newPersonRef, this.educationLineRef);
    }
    public Apprentice withEducationLineRef(DomainId newEducationLineRef) {
        return new Apprentice(this.apprenticeId, this.personRef, newEducationLineRef);
    }

    @Override
    public String toString() {
        return new StringJoiner(",", getClass().getSimpleName() + "{", "}")
            .add("apprenticeId: "+ (apprenticeId == null? null : apprenticeId.value().toString())).add(String.valueOf('\n'))
            .add("personRef: "+ (personRef == null? null : personRef.value().toString())).add(String.valueOf('\n'))
            .add("educationLineRef: "+ (educationLineRef == null? null : educationLineRef.value().toString())).add(String.valueOf('\n'))
        .toString();
    }

    public static Builder builder() {return new Builder();}

    public static class Builder {
        private DomainId apprenticeId;
        private DomainId personRef;
        private DomainId educationLineRef;

        public Builder id(DomainId apprenticeId) {this.apprenticeId = apprenticeId; return this;}
        public Builder personRef(DomainId personRef) {this.personRef = personRef; return this;}
        public Builder educationLineRef(DomainId educationLineRef) {this.educationLineRef = educationLineRef; return this;}

        public Apprentice build() {
            if(apprenticeId == null) this.apprenticeId = ApprenticeId.newId();
            if(personRef == null) throw new ValidationException("personRef.invalid", Map.of("field", "personRef"));
            if(educationLineRef == null) throw new ValidationException("educationLineRef.invalid", Map.of("field", "educationLineRef"));
            return new Apprentice(apprenticeId, personRef, educationLineRef);
        }
    }
}
package local.sop.datawarehouse.consent.domain.model;

import java.util.Map;

import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.valueobject.*;

/* agrregate root */
 
// CHANGED: purpose and type removed. Both describe WHY a statement
// exists and WHETHER it's mandatory — properties of the statement being
// agreed to, not of any individual grant. Two people granting the SAME
// statement can't disagree on its purpose, so those fields belonged on
// ConsentStatement, not here. status stays here, since it genuinely
// varies per grant (person A can withdraw while person B stays active).
public class Consent {
 
    private final DomainId id;
    private final PersonRef personRef;
    private final ConsentStatementRef consentStatementRef;
    private final ConsentStatus status;
 
    private Consent(
        DomainId id,
        PersonRef personRef,
        ConsentStatementRef consentStatementRef,
        ConsentStatus status
    ) {
        this.id = id;
        this.personRef = personRef;
        this.consentStatementRef = consentStatementRef;
        this.status = status;
    }
 
    /* getters */
    public DomainId getId() {
        return id;
    }
 
    public PersonRef getPersonRef() {
        return personRef;
    }
 
    public ConsentStatementRef getConsentStatementRef() {
        return consentStatementRef;
    }
 
    public ConsentStatus getStatus() {
        return status;
    }
 
    /* withers */
    public Consent withStatus(ConsentStatus newStatus) {
        return new Consent(this.id, this.personRef, this.consentStatementRef, newStatus);
    }
 
    /* overrides */
    public String toString() {
        return "Consent{" +
                "id=" + id +
                ", personRef=" + personRef +
                ", consentStatementRef=" + consentStatementRef +
                ", status='" + status + '\'' +
                '}';
    }
 
    /* Builder factory */
     public static Builder builder() { return new Builder(); }
 
    /* Builder inner class */
    public static class Builder {
        private DomainId id;
        private PersonRef personRef;
        private ConsentStatementRef consentStatementRef;
        private ConsentStatus status;
 
        public Builder id(DomainId id) {
            if((id != null && !(id instanceof ConsentId))) {
                throw new ValidationException("key.invalid", Map.of("field", "id"));
            }
            this.id = id;
            return this;
        }
 
        public Builder personRef(PersonRef personRef) {
            this.personRef = personRef;
            return this;
        }
 
        public Builder consentStatementRef(ConsentStatementRef consentStatementRef) {
            this.consentStatementRef = consentStatementRef;
            return this;
        }
 
        public Builder status(ConsentStatus status) {
            this.status = status;
            return this;
        }
 
        public Consent build() {
            if(id == null) { id = ConsentId.newId(); }
            if(personRef == null) { throw new ValidationException("consent.personref.invalid", Map.of("field", "personRef")); }
            if(consentStatementRef == null) { throw new ValidationException("consent.consentstatementref.invalid", Map.of("field", "consentStatementRef")); }
            return new Consent(id, personRef, consentStatementRef, status);
        }
    }
}
 

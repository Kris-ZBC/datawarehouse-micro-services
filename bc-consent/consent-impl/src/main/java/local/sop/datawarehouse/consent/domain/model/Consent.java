package local.sop.datawarehouse.consent.domain.model;

import java.util.Map;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentStatus;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.valueobject.*;

/* agrregate root */

public class Consent {
	
    private final DomainId id;
    private final PersonRef personRef;
    private final ConsentStatementRef consentStatementRef;
    private final ConsentStatus status;
    private final ConsentPurpose purpose;
    private final ConsentType type;

    private Consent(
            DomainId id,
            PersonRef personRef,
            ConsentStatementRef consentStatementRef,
            ConsentStatus status,
            ConsentPurpose purpose, 
            ConsentType type
    ) {
        this.id = id;
        this.personRef = personRef;
        this.consentStatementRef = consentStatementRef;
        this.status = status;
        this.purpose = purpose;
        this.type = type;
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

    public ConsentPurpose getPurpose() {
        return purpose;
    }

    public ConsentType getType() {
        return type;
    }

    /* withers */
    public Consent withStatus(ConsentStatus newStatus) {
        return new Consent(this.id, this.personRef, this.consentStatementRef, newStatus, this.purpose, this.type);
    }

        public Consent withPurpose(ConsentPurpose newPurpose) {
        return new Consent(this.id, this.personRef, this.consentStatementRef,  this.status, newPurpose, this.type);
    }

    public Consent withType(ConsentType newType) {
        return new Consent(this.id, this.personRef, this.consentStatementRef,  this.status, this.purpose, newType);
    }

    /* overrides */
    public String toString() {
        return "Consent{" +
                "id=" + id +
                ", personRef=" + personRef +
                ", consentStatementRef=" + consentStatementRef +
                ", status='" + status + '\'' +
                ", purpose='" + purpose + '\'' +
                ", type='" + type + '\'' +
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
        private ConsentPurpose purpose;
        private ConsentType type;

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

        public Builder purpose(ConsentPurpose purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder type(ConsentType type) {
            this.type = type;
            return this;
        }

        public Consent build() {
            if(id == null) { id = ConsentId.newId(); } 
            if(personRef == null) { throw new ValidationException("consent.personref.invalid", Map.of("field", "personRef")); }
            if(consentStatementRef == null) { throw new ValidationException("consent.consentstatementref.invalid", Map.of("field", "consentStatementRef")); }
            return new Consent(id, personRef, consentStatementRef, status, purpose, type);
        }
    }
}

package local.sop.datawarehouse.consent.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementValue;

/* aggregate root */

public class ConsentStatement {

    private final DomainId id;
    private final boolean active;
    private final ConsentStatementValue statementText;
    private final ConsentPurpose purpose;
    private final ConsentType type;
    private final Set<Consent> consents; 

    private ConsentStatement(DomainId id, boolean active, ConsentStatementValue statementText,ConsentPurpose purpose, ConsentType type, Set<Consent> consents) {
        this.id = id;
        this.active = active;
        this.statementText = statementText;
        this.purpose = purpose;
        this.type = type;
        this.consents = consents;
    }

    /* getters */

    public DomainId getId() {
        return id;
    }

    public String getStatementText() {
        return statementText.value();
    }

    public boolean isActive() {
        return active;
    }

    public ConsentPurpose getPurpose() {
        return purpose;
    }
 
    public ConsentType getType() {
        return type;
    }

    public Set<Consent> getConsents() {
        return Collections.unmodifiableSet(consents != null ? consents : Set.of());
    }

    /* withers */
    public ConsentStatement withStatementText(ConsentStatementValue newStatementText) {
        return new ConsentStatement(this.id, this.active, newStatementText, this.purpose, this.type, this.consents);
    }
 
    public ConsentStatement withActive(boolean newActive) {
        return new ConsentStatement(this.id, newActive, this.statementText, this.purpose, this.type, this.consents);
    }
 
    public ConsentStatement withPurpose(ConsentPurpose newPurpose) {
        return new ConsentStatement(this.id, this.active, this.statementText, newPurpose, this.type, this.consents);
    }
 
    public ConsentStatement withType(ConsentType newType) {
        return new ConsentStatement(this.id, this.active, this.statementText, this.purpose, newType, this.consents);
    }
 
    public ConsentStatement withConsents(Set<Consent> newConsents) {
        return new ConsentStatement(this.id, this.active, this.statementText, this.purpose, this.type, newConsents);
    }
 
    /* overrides */
    public String toString() {
        return "ConsentStatement{" +
                "id=" + id +
                ", active=" + active +
                ", statementText='" + statementText.value() + '\'' +
                ", purpose=" + purpose +
                ", type=" + type +
                ", consents=" + consents +
                '}';
    }
 
    /* builder */
    public static Builder builder() {
        return new Builder();
    }
 
    /* Builder inner class */
    public static class Builder {
        private DomainId id;
        private boolean active = true;
        private ConsentStatementValue statementText;
        private ConsentPurpose purpose;
        private ConsentType type;
        private Set<Consent> consents;
 
        public Builder id(DomainId id) {
            if (!(id instanceof ConsentStatementRef)) {
                throw new ValidationException("key.invalid", Map.of("field", "id"));
            }
            this.id = id;
            return this;
        }
 
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }
 
        public Builder statementText(ConsentStatementValue statementText) {
            this.statementText = statementText;
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
 
        public Builder consents(Set<Consent> consents) {
            this.consents = consents;
            return this;
        }
 
        public ConsentStatement build() {
            if(id == null) { id = ConsentStatementRef.newId(); }
            if(consents == null) { consents = new HashSet<>(); }
            if(statementText == null) { throw new ValidationException("consentstatement.statementtext.invalid", Map.of("field", "statementText")); }
            if(purpose == null) { throw new ValidationException("consentstatement.purpose.invalid", Map.of("field", "purpose")); }
            if(type == null) { throw new ValidationException("consentstatement.type.invalid", Map.of("field", "type")); }
            return new ConsentStatement(id, active, statementText, purpose, type, consents);
        }
    }
}
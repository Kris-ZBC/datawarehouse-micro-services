package local.sop.sopinfo.consent.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

/* aggregate root */

public class ConsentStatement {

    private final DomainId id;
    private final boolean active;
    private final ConsentStatementValue statementText;
    private final Set<Consent> consents; 

    private ConsentStatement(DomainId id, boolean active, ConsentStatementValue statementText, Set<Consent> consents) {
        this.id = id;
        this.active = active;
        this.statementText = statementText;
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

    public Set<Consent> getConsents() {
        return Collections.unmodifiableSet(consents != null ? consents : Set.of());
    }

    /* withers */
    public ConsentStatement withStatementText(ConsentStatementValue newStatementText) {
        return new ConsentStatement(this.id, this.active, newStatementText, this.consents);
    }

    public ConsentStatement withActive(boolean newActive) {
        return new ConsentStatement(this.id, newActive, this.statementText, this.consents);
    }

    public ConsentStatement withConsents(Set<Consent> newConsents) {
        return new ConsentStatement(this.id, this.active, this.statementText, newConsents);
    }

    /* overrides */
    public String toString() {
        return "ConsentStatement{" +
                "id=" + id +
                ", active=" + active +
                ", statementText='" + statementText.value() + '\'' +
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

        public Builder consents(Set<Consent> consents) {
            this.consents = consents;
            return this;
        }

        public ConsentStatement build() {
            if(id == null) { id = ConsentStatementRef.newId(); }
            if(consents == null) { consents = new HashSet<>(); }
            if(statementText == null) { throw new ValidationException("consentstatement.statementtext.invalid", Map.of("field", "statementText")); }
            return new ConsentStatement(id, active, statementText, consents);
        }
    }
}
package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consent;

import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;
import local.sop.sopinfo.sharedkernel.enums.ConsentPurpose;
import local.sop.sopinfo.sharedkernel.enums.ConsentStatus;
import local.sop.sopinfo.sharedkernel.enums.ConsentType;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "consent", uniqueConstraints = @UniqueConstraint(columnNames = {"person_reference", "consent_statement_id"}))
public class ConsentEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "person_reference", nullable = false)
    private UUID personReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_statement_id", nullable = false)
    private ConsentStatementEntity consentStatement;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsentStatus status;

     @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private ConsentPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ConsentType type;

    protected ConsentEntity() {
        // JPA kræver en default constructor
    }

    private ConsentEntity(
            UUID id,
            UUID personReference,
            ConsentStatementEntity consentStatement,
            ConsentStatus status,
             ConsentPurpose purpose, 
             ConsentType type
        ) {
        this.id = id;
        this.personReference = personReference;
        this.consentStatement = consentStatement;
        this.status = status;
        this.purpose = purpose;
        this.type = type;
    }

    /* Withers */
    public ConsentEntity withConsentStatement(ConsentStatementEntity consentStatement) {
        this.consentStatement = consentStatement;
        return this;
    }

    public ConsentEntity withStatus(ConsentStatus status) {
        this.status = status;
        return this;
    }

    public ConsentEntity withConsentPurpose(ConsentPurpose consentPurpose) {
        this.purpose = consentPurpose;
        return this;
    }

    public ConsentEntity withConsentType(ConsentType consentType) {
        this.type = consentType;
        return this;
    }


    /* Getters */

    public UUID getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getPersonReference() {
        return personReference;
    }

    public ConsentStatementEntity getConsentStatement() {
        return consentStatement;
    }
    public ConsentStatus getStatus() {
        return status;
    }

        public ConsentPurpose getConsentPurpose() {
        return purpose;
    }

    public ConsentType getConsentType() {
        return type;
    }   

    /* Buiilder factory */
    public static Builder builder() { return new Builder(); }

    /* Builder inner class */
    public static class Builder {
        private UUID id;
        private UUID personReference;
        private ConsentStatementEntity consentStatement;
        private ConsentStatus status;
        private ConsentPurpose consentPurpose;
        private ConsentType consentType;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder personReference(UUID personReference) {
            this.personReference = personReference;
            return this;
        }

        public Builder consentStatement(ConsentStatementEntity consentStatement) {
            this.consentStatement = consentStatement;
            return this;
        }

        public Builder status(ConsentStatus status) {
            this.status = status;
            return this;
        }

        public Builder consentPurpose(ConsentPurpose consentPurpose) {
            this.consentPurpose = consentPurpose;
            return this;
        }

        public Builder consentType(ConsentType consentType) {
            this.consentType = consentType;
            return this;
        }

        public ConsentEntity build() {
            if(id == null) { throw new ValidationException("key.invalid", Map.of("field", "id")); }
            if(consentStatement == null) { throw new ValidationException("consent.consentstatement.invalid", Map.of("field", "consentStatement")); }
            return new ConsentEntity(id, personReference, consentStatement, status, consentPurpose, consentType);
        }
    }
}
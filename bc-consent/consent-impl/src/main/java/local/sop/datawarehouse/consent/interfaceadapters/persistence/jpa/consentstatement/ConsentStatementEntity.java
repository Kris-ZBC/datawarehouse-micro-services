package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consent.ConsentEntity;

@Entity
@Table(name = "consent_statement")
public class ConsentStatementEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "statement_text", nullable = false, length = 1000)
    private String statementText;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // CHANGED: moved from ConsentEntity. Every Consent granted against
    // this statement necessarily shares the same purpose/type, so it
    // belongs here rather than being repeated (and potentially
    // inconsistent) on every individual grant.
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private ConsentPurpose purpose;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ConsentType type;
    
    @OneToMany(mappedBy = "consentStatement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConsentEntity> consents = new HashSet<>();

    protected ConsentStatementEntity() {
        // JPA requires a default constructor
    }

    private ConsentStatementEntity(UUID id, String statementText, boolean active,
            ConsentPurpose purpose, ConsentType type, Set<ConsentEntity> consents) {
        this.id = id;
        this.statementText = statementText;
        this.active = active;
        this.purpose = purpose;
        this.type = type;
        this.consents = consents;
    }
 
    /* Getters */
    public UUID getId() {
        return id;
    }
 
    public Long getVersion() {
        return version;
    }
 
    public String getStatementText() {
        return statementText;
    }
 
    public ConsentPurpose getPurpose() {
        return purpose;
    }
 
    public ConsentType getType() {
        return type;
    }
 
    public Set<ConsentEntity> getConsents() {
        return Collections.unmodifiableSet(consents != null ? consents : Set.of());
    }
 
 
    public boolean isActive() {
        return active;
    }
 
    /* Withers */
    public ConsentStatementEntity withStatementText(String statementText) {
        this.statementText = statementText;
        return this;
    }
 
    public ConsentStatementEntity withPurpose(ConsentPurpose purpose) {
        this.purpose = purpose;
        return this;
    }
 
    public ConsentStatementEntity withType(ConsentType type) {
        this.type = type;
        return this;
    }
 
    public ConsentStatementEntity withConsents(Set<ConsentEntity> consents) {
        this.consents = consents;
        return this;
    }
 
    public ConsentStatementEntity withActive(boolean active) {
        this.active = active;
        return this;
    }
 
 
    /* Builder factory */
    public static ConsentStatementEntity.Builder builder() {
        return new ConsentStatementEntity.Builder();
    }
 
    /* Builder inner class */
    public static class Builder {
        private UUID id;
        private String statementText;
        private boolean active = true;
        private ConsentPurpose purpose;
        private ConsentType type;
        private Set<ConsentEntity> consents;
 
 
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
 
        public Builder statementText(String statementText) {
            this.statementText = statementText;
            return this;
        }
 
        public Builder active(boolean active) {
            this.active = active;
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
 
        public Builder consents(Set<ConsentEntity> consents) {
            this.consents = consents;
            return this;
        }
 
        public ConsentStatementEntity build() {
            if(id == null) throw new ValidationException("key.invalid", Map.of("field", "id"));
            if(statementText == null) throw new ValidationException("consentstatement.statementtext.invalid", Map.of("field", "statementText"));
            if(purpose == null) throw new ValidationException("consentstatement.purpose.invalid", Map.of("field", "purpose"));
            if(type == null) throw new ValidationException("consentstatement.type.invalid", Map.of("field", "type"));
            if(consents == null) { consents = new HashSet<>(); }
            return new ConsentStatementEntity(id, statementText, active, purpose, type, consents);
        }
    }
}
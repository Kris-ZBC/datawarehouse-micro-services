package local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consent;

import java.util.Map;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.consent.domain.model.Consent;
import local.sop.sopinfo.consent.domain.model.valueobject.ConsentId;
import local.sop.sopinfo.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.sopinfo.consent.domain.model.valueobject.PersonRef;
import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.ConsentStatementEntity;
import local.sop.sopinfo.consent.interfaceadapters.persistence.jpa.consentstatement.factory.StatementEntityFactory;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Component
public class ConsentJpaMapper {

    private final StatementEntityFactory statementFactory;


    public ConsentJpaMapper(StatementEntityFactory statementFactory) {
        this.statementFactory = statementFactory;
    }

    public Consent toDomain(ConsentEntity entity) {
        if (entity == null) {
            return null;
        }

        if (entity.getConsentStatement() == null) {
            throw new ValidationException("consent.statement.required", 
                Map.of("field", "consentStatement"));
        }

        if (entity.getConsentStatement().getId() == null) {
            throw new ValidationException("key.required", 
                Map.of("field", "consentStatement.id"));
        }

        return Consent.builder()
                .id(entity.getId() != null ? new ConsentId(entity.getId()) : null)
                .personRef(entity.getPersonReference() != null ? 
                    new PersonRef(entity.getPersonReference()) : null)
                .consentStatementRef(new ConsentStatementRef(entity.getConsentStatement().getId()))
                .status(entity.getStatus())
                .purpose(entity.getConsentPurpose())
                .type(entity.getConsentType())
                .build();
    }

    public ConsentEntity toEntity(Consent domain) {
        if (domain == null) {
            return null;
        }

        if (domain.getId() == null) {
            throw new ValidationException("key.required", 
                Map.of("field", "id"));
        }

        if (domain.getPersonRef() == null) {
            throw new ValidationException("key.required", 
                Map.of("field", "personRef"));
        }

        ConsentStatementEntity statementEntity = statementFactory.createStatementEntity(domain.getConsentStatementRef());

        return ConsentEntity.builder()   
            .id(domain.getId().value())
            .personReference(domain.getPersonRef().value())
            .consentStatement(statementEntity)
            .status(domain.getStatus())
            .consentPurpose(domain.getPurpose())
            .consentType(domain.getType())
            .build();
    }


}
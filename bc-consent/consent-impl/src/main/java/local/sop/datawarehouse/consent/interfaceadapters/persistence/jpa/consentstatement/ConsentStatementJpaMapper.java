package local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consentstatement;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import local.sop.datawarehouse.consent.domain.model.Consent;
import local.sop.datawarehouse.consent.domain.model.ConsentStatement;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentId;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementRef;
import local.sop.datawarehouse.consent.domain.model.valueobject.ConsentStatementValue;
import local.sop.datawarehouse.consent.domain.model.valueobject.PersonRef;
import local.sop.datawarehouse.consent.interfaceadapters.persistence.jpa.consent.ConsentEntity;


@Component
public class ConsentStatementJpaMapper {

    public ConsentStatement toDomain(ConsentStatementEntity entity) {
        if (entity == null) return null;
        
        return ConsentStatement.builder()
                .id(new ConsentStatementRef(entity.getId()))
                .statementText(new ConsentStatementValue(entity.getStatementText()))
                .active(entity.isActive())
                .purpose(entity.getPurpose())
                .type(entity.getType())
                .consents(entity.getConsents() != null ? entity.getConsents().stream()
                    .map(this::mapConsentEntityToDomain)
                    .collect(Collectors.toSet()) : Set.of())
                .build();
    }

    // Separat metode for consent mapping
    private Consent mapConsentEntityToDomain(ConsentEntity consentEntity) {
        if (consentEntity == null) return null;
        
        return Consent.builder()
                .id(new ConsentId(consentEntity.getId()))
                .personRef(new PersonRef(consentEntity.getPersonReference()))
                .consentStatementRef(new ConsentStatementRef(consentEntity.getConsentStatement().getId()))
                .status(consentEntity.getStatus())
                .build();
    }

    public ConsentStatementEntity toEntity(ConsentStatement domain) {
        if (domain == null) return null;

        // Opret parent entity først
        ConsentStatementEntity entity = ConsentStatementEntity.builder()
                .id(domain.getId().value())
                .statementText(domain.getStatementText())
                .active(domain.isActive())
                .purpose(domain.getPurpose())
                .type(domain.getType())
                .build();

        // Map consents med reference til parent entity
        Set<ConsentEntity> consentEntities = domain.getConsents() != null ?
            domain.getConsents().stream()
                .filter(Objects::nonNull)
                .map(consent -> mapConsentToEntity(consent, entity))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
            : Set.of();

        // Brug withConsents() i stedet for at bygge ny entity
        return entity.withConsents(consentEntities);
    }

    // Separat metode for consent til entity mapping
    private ConsentEntity mapConsentToEntity(Consent consent, ConsentStatementEntity parentEntity) {
        if (consent == null || parentEntity == null) return null;

        return ConsentEntity.builder()
                .id(consent.getId().value())
                .personReference(consent.getPersonRef().value())
                .consentStatement(parentEntity) // ← Reference til parent
                .status(consent.getStatus())
                .build();
    }

}
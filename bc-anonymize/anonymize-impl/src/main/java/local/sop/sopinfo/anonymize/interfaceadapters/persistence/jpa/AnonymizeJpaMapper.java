package local.sop.sopinfo.anonymize.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.AnonymizeId;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;

@Component
public final class AnonymizeJpaMapper {

    public AnonymizeJpaMapper() {
    }

    public Anonymize toDomain(AnonymizeEntity entity) {
        return Anonymize.of(
                AnonymizeId.of(entity.getId()),
                PersonRef.of(entity.getPersonRef())
        );
    }

    public AnonymizeEntity toEntity(Anonymize anonymizeLog) {
        return AnonymizeEntity.create(
                anonymizeLog.getAnonymizationId().value(),
                anonymizeLog.getPersonRef().value()
        );

    }
}
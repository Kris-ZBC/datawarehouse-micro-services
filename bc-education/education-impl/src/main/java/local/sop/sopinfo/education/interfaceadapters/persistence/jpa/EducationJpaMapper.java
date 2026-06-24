package local.sop.sopinfo.education.interfaceadapters.persistence.jpa;

import java.util.List;

import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.*;

public class EducationJpaMapper {

    public Education toDomain(EducationEntity entity) {
        return Education.builder()
                .id(new EducationId(entity.getId()))
                .name(new EducationName(entity.getName()))
                .category(new EducationCategory(entity.getCategory()))
                .active(entity.isActive())
                .build();
    }

    public EducationEntity toEntity(Education education) {
        return new EducationEntity(
                education.getId().value(),
                education.getName().value(),
                education.getCategory().value(),
                education.isActive()
        );
    }

    public List<Education> toDomainList(List<EducationEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
}
package local.sop.sopinfo.sopeducation.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;
import local.sop.sopinfo.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;

@Component
public class SopEducationDomainJpaMapper {

    /** JPA -> Domain  */
    public SopEducation toDomain(SopEducationEntity e) {
        return SopEducation.builder()
            .id(new CompositeKey(e.getId().getSopRef(), e.getId().getEducationRef()))
            .active(e.isActive())
            .createdAt(new CreatedAtTimestamp(e.getCreatedAt()))
            .build();
    }

    /** Domain -> JPA */

    /* preconditions 
     * SopEducationLine must have a valid CompositeKey id with non-null key1 and key2
     * Key1 equals sopRef and key2 equals educationLineRef in the SopEducationLineEntity 
    */
    public SopEducationEntity toEntity(SopEducation s) {
        return SopEducationEntity.builder()
            .id(new SopEducationId(s.getId().key1(), s.getId().key2()))
            .active(s.isActive())
            .build();
    }

    /** Domain -> existing JPA (Update active field) */
    public void updateEntity(SopEducation s, SopEducationEntity se) {
        se.withActive(s.isActive());
    }
}

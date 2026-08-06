package local.sop.datawarehouse.instructor.interfaceadapters.persistence.jpa;

import local.sop.datawarehouse.instructor.domain.model.Instructor;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.InstructorId;
import local.sop.datawarehouse.instructor.domain.model.valueobjects.PersonRef;

public final class InstructorJpaMapper {

    private InstructorJpaMapper() {
    }

    public static Instructor toDomain(InstructorEntity entity) {
        return Instructor.builder()
                .id(InstructorId.of(entity.getId()))
                .personRef(PersonRef.of(entity.getPersonRef()))
                .build();
    }

    public static InstructorEntity toEntity(Instructor instructor) {
        InstructorEntity entity = new InstructorEntity();
        entity.setId(instructor.getId().value());
        entity.setPersonRef(instructor.getPersonRef().value());
        return entity;
    }
}
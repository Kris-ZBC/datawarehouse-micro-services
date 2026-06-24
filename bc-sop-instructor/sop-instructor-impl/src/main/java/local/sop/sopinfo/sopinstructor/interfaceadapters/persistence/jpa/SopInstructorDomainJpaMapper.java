package local.sop.sopinfo.sopinstructor.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.sopinstructor.domain.model.SopInstructor;
import local.sop.sopinfo.sopinstructor.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;

@Component
public class SopInstructorDomainJpaMapper {
    /** JPA -> Domain */
    public SopInstructor toDomain(SopInstructorEntity e) {
        return new SopInstructor.Builder()
            .id(new CompositeKey(e.getId().getSopRef(), e.getId().getInstructorRef()))
            .active(e.isActive())
            .createdAt(new CreatedAtTimestamp(e.getCreatedAt()))
            .build();
    }

    /** Domain -> JPA */

    /* preconditions
    * SopInstructor must have a valid Compositekey id with non-null key1 and key2
    * Key1 equals sopRef and Key2 equals instructorRef in the SopInstructorEntity */
    public SopInstructorEntity toEntity(SopInstructor s) {
        return SopInstructorEntity.builder()
            .id(new SopInstructorId(s.getId().key1(), s.getId().key2()))
            .active(s.isActive())
            .build();

    }

    /* Domain -> existing JPA entity */
    public void updateIntoEntity(SopInstructor s, SopInstructorEntity me) {
        me.withActive(s.isActive());

    }

}

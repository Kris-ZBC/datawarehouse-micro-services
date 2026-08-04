package local.sop.sopinfo.educationinstructor.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.educationinstructor.domain.model.EducationInstructor;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;

@Component
public class EducationInstructorJpaMapper {
	public EducationInstructor toDomain(EducationInstructorEntity entity) {
		return new EducationInstructor.Builder()
			.id(new CompositeKey(entity.getId().getEducationRef(), entity.getId().getInstructorRef()))
			.createdAt(new CreatedAtTimestamp(entity.getCreatedAt()))
			.active(entity.isActive())
			.build();
	}

	public EducationInstructorEntity toEntity(EducationInstructor domain) {
		return new EducationInstructorEntity.Builder()
			.id(new EducationInstructorId(domain.getId().key1(), domain.getId().key2()))
			.active(domain.isActive())
			.build();
	}

	public void updateIntoEntity(EducationInstructor domain, EducationInstructorEntity entity) {
		entity.withActive(domain.isActive());
	}
}

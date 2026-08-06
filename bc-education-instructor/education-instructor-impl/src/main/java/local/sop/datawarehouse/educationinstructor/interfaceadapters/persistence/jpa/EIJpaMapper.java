package local.sop.datawarehouse.educationinstructor.interfaceadapters.persistence.jpa;

import org.springframework.stereotype.Component;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.educationinstructor.domain.model.EducationInstructor;
import local.sop.datawarehouse.educationinstructor.domain.model.valueobjects.CreatedAtTimestamp;

@Component
public class EIJpaMapper {
	public EducationInstructor toDomain(EIEntity entity) {
		return new EducationInstructor.Builder()
			.id(new CompositeKey(entity.getId().getEducationRef(), entity.getId().getInstructorRef()))
			.createdAt(new CreatedAtTimestamp(entity.getCreatedAt()))
			.active(entity.isActive())
			.build();
	}

	public EIEntity toEntity(EducationInstructor domain) {
		return new EIEntity.Builder()
			.id(new EIId(domain.getId().key1(), domain.getId().key2()))
			.active(domain.isActive())
			.build();
	}

	public void updateIntoEntity(EducationInstructor domain, EIEntity entity) {
		entity.withActive(domain.isActive());
	}
}

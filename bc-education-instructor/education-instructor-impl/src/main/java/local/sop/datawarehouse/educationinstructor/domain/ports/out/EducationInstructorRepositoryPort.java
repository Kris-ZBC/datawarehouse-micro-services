package local.sop.datawarehouse.educationinstructor.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.educationinstructor.domain.model.EducationInstructor;

public interface EducationInstructorRepositoryPort {
	public EducationInstructor save(EducationInstructor educationInstructor);
	public List<EducationInstructor> findAll();
	public Optional<EducationInstructor> findById(CompositeKey id);
	public List<EducationInstructor> findByEducationRef(UUID educationRef);
	public List<EducationInstructor> findByInstructorRef(UUID instructorRef);
	public void update(EducationInstructor educationInstructor);

	// Compensate methods
	public Boolean compensateCreateEducationInstructor(CompositeKey id, SagaOutcome sagaState);
	public Boolean compensateActivateEducationInstructor(CompositeKey id, SagaOutcome sagaState);
	public Boolean compensateDeactivateEducationInstructor(CompositeKey id, SagaOutcome sagaState);
}

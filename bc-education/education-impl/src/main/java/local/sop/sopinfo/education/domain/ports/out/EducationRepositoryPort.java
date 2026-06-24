package local.sop.sopinfo.education.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationCategory;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationId;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationName;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface EducationRepositoryPort {
    boolean existsByName(EducationName name);
    boolean existsByCategory(EducationCategory category);
    Education create(Education education);
    Education updateName(Education education);
    Education updateCategory(Education education);
    Optional<Education> findById(EducationId id);
    List<Education> findAll();
    Education activate(Education education);
    Education deactivate(Education education);
    
    // Compensate

    Boolean compensateCreate(EducationId id, SagaOutcome sagaState);
    Boolean compensateActivate(EducationId id, SagaOutcome sagaState);
    Boolean compensateDeactivate(EducationId id, SagaOutcome sagaState);
    Boolean compensateUpdateName(EducationId id, SagaOutcome sagaState, String oldName);
    Boolean compensateUpdateCategory(EducationId id, SagaOutcome sagaState, String oldCategory);
    
}
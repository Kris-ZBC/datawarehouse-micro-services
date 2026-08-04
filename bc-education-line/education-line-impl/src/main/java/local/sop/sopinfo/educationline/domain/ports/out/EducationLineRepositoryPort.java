package local.sop.sopinfo.educationline.domain.ports.out;

import java.util.List;
import java.util.Optional;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

public interface EducationLineRepositoryPort {
	public EducationLine save(EducationLine educationLine);
	public List<EducationLine> findAll();
	public Optional<EducationLine> findById(EducationLineId id);
	public List<EducationLine> findByEducationRef(EducationRef educationRef);
	public Optional<EducationLine> updateEducationLineName(EducationLineId id, EducationLineName name);
	public Optional<EducationLine> updateEducationLineDuration(EducationLineId id, EducationLineDuration duration);
	public Optional<EducationLine> deactivate(EducationLineId id);
	public Optional<EducationLine> activate(EducationLineId id);
	// ONLY for compensation
	public Boolean compensate(EducationLineId id, SagaOutcome sagaState);
	public Boolean compensateActivate(EducationLineId id, SagaOutcome saga);
	public Boolean compensateDeactivate(EducationLineId id, SagaOutcome saga);
	public Boolean compensateName(EducationLineId id, SagaOutcome saga, EducationLineName nameReq);
	public Boolean compensateDuration(EducationLineId id, SagaOutcome saga, EducationLineDuration req);

}

package local.sop.sopinfo.educationline.saga.application.api;

import java.util.UUID;

import local.sop.sopinfo.educationline.saga.application.api.dto.CreateAuditlogCmd;
import local.sop.sopinfo.educationline.saga.application.api.dto.CreateEducationLineCmd;
import local.sop.sopinfo.educationline.saga.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.saga.application.api.dto.UpdateEducationLineDurationCmd;
import local.sop.sopinfo.educationline.saga.application.api.dto.UpdateEducationLineNameCmd;

public interface EducationLineSagaDirectory {
    EducationLineResponse createEducationLine(CreateEducationLineCmd cmd);
    EducationLineResponse updateEducationLineName(UUID id, UpdateEducationLineNameCmd cmd);
    EducationLineResponse updateEducationLineDuration(UUID id, UpdateEducationLineDurationCmd cmd);
    EducationLineResponse deactivateEducationLine(UUID id, CreateAuditlogCmd cmd);
	EducationLineResponse activateEducationLine(UUID id, CreateAuditlogCmd cmd);
}

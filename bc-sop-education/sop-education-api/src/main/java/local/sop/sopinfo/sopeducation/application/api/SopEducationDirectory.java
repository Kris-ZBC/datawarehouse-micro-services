package local.sop.sopinfo.sopeducation.application.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sopeducation.application.api.dto.CreateSopEducationCmd;
import local.sop.sopinfo.sopeducation.application.api.dto.CreatedSopEducationResult;
import local.sop.sopinfo.sopeducation.application.api.dto.SopEducationResponse;
import local.sop.sopinfo.sopeducation.application.api.dto.ToggleActivateSopEducationCmd;

public interface SopEducationDirectory {
    CreatedSopEducationResult create(CreateSopEducationCmd command);
    Optional<SopEducationResponse> findById(CompositeKey id);
    SopEducationResponse toggleActive(ToggleActivateSopEducationCmd command);
    List<SopEducationResponse> getBySopRef(UUID sopRef);
    List<SopEducationResponse> getByEducationRef(UUID educationRef);

}

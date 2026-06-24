package local.sop.sopinfo.educationline.saga.application.ports.out.education;

import java.util.UUID;

import local.sop.sopinfo.educationline.saga.application.api.dto.EducationResponse;

public interface EducationPort {
    EducationResponse existsById(UUID id);
}

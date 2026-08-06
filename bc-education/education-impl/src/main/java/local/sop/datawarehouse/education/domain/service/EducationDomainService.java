package local.sop.datawarehouse.education.domain.service;

import local.sop.datawarehouse.education.domain.model.Education;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationId;

public class EducationDomainService implements EducationDomain {

    @Override
    public Education createEducation(Education education) {
        // Domain logic for creating an education can be added here
        return Education.builder()
                .id(EducationId.newId())
                .name(education.getName())
                .category(education.getCategory())
                .active(false)
                .build();
    }
}
package local.sop.sopinfo.educationline.domain.service;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;

public class EducationLineDomainService implements EducationLineDomain {

	@Override
	public EducationLine createEducationLine(EducationLine educationLine) {
		// Domain logic for creating an education line can be added here
		return EducationLine.builder()
				.id(EducationLineId.newId())
				.name(educationLine.getName())
				.duration(educationLine.getDuration())
				.createdAt(educationLine.getCreatedAt())
				.educationRef(educationLine.getEducationRef())
				.active(educationLine.isActive())
				.build();
	}
	
}

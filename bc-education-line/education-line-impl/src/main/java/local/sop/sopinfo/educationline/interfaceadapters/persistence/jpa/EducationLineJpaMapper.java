package local.sop.sopinfo.educationline.interfaceadapters.persistence.jpa;

import java.util.List;

import local.sop.sopinfo.educationline.domain.model.EducationLine;
import local.sop.sopinfo.educationline.domain.model.valueobjects.*;

public class EducationLineJpaMapper {
	public EducationLine toDomain(EducationLineEntity entity) {
		return EducationLine.builder()
				.id(new EducationLineId(entity.getId()))
				.name(new EducationLineName(entity.getName()))
				.duration(new EducationLineDuration(entity.getDurationYears(), entity.getDurationMonths(), entity.getDurationDays()))
				.educationRef(new EducationRef(entity.getEducationRef()))
				.active(entity.isActive())
				.build();
	}

	public EducationLineEntity toEntity(EducationLine educationLine) {
		return new EducationLineEntity(
				educationLine.getId().value(),
				educationLine.getName().value(),
				educationLine.getDuration().getDurationYears(),
				educationLine.getDuration().getDurationMonths(),
				educationLine.getDuration().getDurationDays(),
				educationLine.getEducationRef().value(),
				educationLine.isActive()
		);
	}

	public List<EducationLine> toDomainList(List<EducationLineEntity> entities) {
		return entities.stream()
				.map(this::toDomain)
				.toList();
	}
}

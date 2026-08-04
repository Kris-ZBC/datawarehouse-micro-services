package local.sop.sopinfo.educationline.domain.model;

import java.util.Map;

import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineCreatedAt;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineDuration;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineId;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationLineName;
import local.sop.sopinfo.educationline.domain.model.valueobjects.EducationRef;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class EducationLine {
	
	// EducationLine properties
	private final EducationLineId id;
	private final EducationLineName name;
	private final EducationLineDuration duration;
	private final EducationLineCreatedAt createdAt;
	private final EducationRef educationRef;
	private final Boolean active;

	public EducationLine(EducationLineId id, EducationLineName name, EducationLineDuration duration, EducationLineCreatedAt createdAt, EducationRef educationRef, Boolean active) {
		this.id = requireNonNull(id, "educationline.id.required", "educationLineId");
		this.name = requireNonNull(name, "educationline.name.required", "educationLineName");
		this.duration = requireNonNull(duration, "educationline.duration.required", "educationLineDuration");
		this.createdAt = requireNonNull(createdAt, "educationline.createdat.required", "educationLineCreatedAt");
		this.educationRef = requireNonNull(educationRef, "educationline.educationref.required", "educationRef");
		this.active = requireNonNull(active, "educationline.active.required", "active");
	}

	public static EducationLine create(EducationLineId id, EducationLineName name, EducationLineDuration duration, EducationLineCreatedAt createdAt, EducationRef educationRef, Boolean active) {
		EducationLineId actualId = id != null ? id : EducationLineId.newId();
		EducationLineCreatedAt actualCreatedAt = createdAt != null ? createdAt : EducationLineCreatedAt.now();
		return new EducationLine(actualId, name, duration, actualCreatedAt, educationRef, active);
	}

	public EducationLineId getId() {
		return id;
	}

	public EducationLineName getName() {
		return name;
	}

	public EducationLineDuration getDuration() {
		return duration;
	}

	public EducationLineCreatedAt getCreatedAt() {
		return createdAt;
	}

	public EducationRef getEducationRef() {
		return educationRef;
	}

	public Boolean isActive() {
		return active;
	}

	public static Builder builder() {
		return new Builder();
	}

	// Saw this method in auditlog's domain model, maybe we can add it in sharedkernel?
	private static <T> T requireNonNull(T value, String messageKey, String field) {
		if (value == null) {
			throw new ValidationException(messageKey, Map.of("field", field));
		}
		return value;
	}

	public static class Builder {
		private EducationLineId id;
		private EducationLineName name;
		private EducationLineDuration duration;
		private EducationLineCreatedAt createdAt;
		private EducationRef educationRef;
		private Boolean active;

		public Builder id(EducationLineId id) {
			this.id = id;
			return this;
		}

		public Builder name(EducationLineName name) {
			this.name = name;
			return this;
		}

		public Builder duration(EducationLineDuration duration) {
			this.duration = duration;
			return this;
		}

		public Builder createdAt(EducationLineCreatedAt createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder educationRef(EducationRef educationRef) {
			this.educationRef = educationRef;
			return this;
		}

		public Builder active(Boolean active) {
			this.active = active;
			return this;
		}

		public EducationLine build() {
			return EducationLine.create(id, name, duration, createdAt, educationRef, active);
		}
	}
}

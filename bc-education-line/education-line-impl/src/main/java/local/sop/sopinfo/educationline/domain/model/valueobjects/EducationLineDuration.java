package local.sop.sopinfo.educationline.domain.model.valueobjects;

import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record EducationLineDuration(int years, int months, int days) {
	public EducationLineDuration {
		if (years < 0) {
			throw new ValidationException("educationline.duration.years.invalid", Map.of("field", "EducationLineDuration.years"));
		}
		if (months < 0 || months > 11) {
			throw new ValidationException("educationline.duration.months.invalid", Map.of("field", "EducationLineDuration.months"));
		}
		if (days < 0 || days > 30) {
			throw new ValidationException("educationline.duration.days.invalid", Map.of("field", "EducationLineDuration.days"));
		}

		if (years == 0 && months == 0 && days == 0) {
			throw new ValidationException("educationline.duration.invalid", Map.of("field", "EducationLineDuration"));
		}
	}

	public int getDurationYears() {
		return years;
	}

	public int getDurationMonths() {
		return months;
	}

	public int getDurationDays() {
		return days;
	}

	public int totalMonths() {
		return years * 12 + months;
	}

	public int totalDays() {
		return totalMonths() * 30 + days;
	}

	public String asString() {
		StringBuilder sb = new StringBuilder();
		if (years > 0) {
			sb.append(years).append(" year").append(years > 1 ? "s" : "");
		}
		if (months > 0) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(months).append(" month").append(months > 1 ? "s" : "");
		}
		if (days > 0) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(days).append(" day").append(days > 1 ? "s" : "");
		}
		return sb.toString();
	}
	
}

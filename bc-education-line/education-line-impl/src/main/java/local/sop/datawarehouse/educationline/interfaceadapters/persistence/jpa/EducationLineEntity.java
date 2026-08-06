package local.sop.datawarehouse.educationline.interfaceadapters.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Id;

@Entity
@Table(name = "education_line")
public class EducationLineEntity {
	@Id
	private UUID id;

	@Version
    private Long version;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "duration_years", nullable = false)
	private Integer durationYears;

	@Column(name = "duration_months", nullable = false)
	private Integer durationMonths;

	@Column(name = "duration_days", nullable = false)
	private Integer durationDays;

	@Column(name = "education_ref", nullable = false)
	private UUID educationRef;

	@Column(name = "is_active", nullable = false)
	private Boolean active;

	protected EducationLineEntity() { } // JPA requires a default constructor

	public EducationLineEntity(UUID id, String name, Integer durationYears, Integer durationMonths, Integer durationDays, UUID educationRef, Boolean active) {
		this.id = id;
		this.name = name;
		this.durationYears = durationYears;
		this.durationMonths = durationMonths;
		this.durationDays = durationDays;
		this.educationRef = educationRef;
		this.active = active;
	}

	// Getters and setters
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDurationYears() {
		return durationYears;
	}

	public void setDurationYears(Integer durationYears) {
		this.durationYears = durationYears;
	}

	public Integer getDurationMonths() {
		return durationMonths;
	}

	public void setDurationMonths(Integer durationMonths) {
		this.durationMonths = durationMonths;
	}

	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}

	public UUID getEducationRef() {
		return educationRef;
	}

	public void setEducationRef(UUID educationRef) {
		this.educationRef = educationRef;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
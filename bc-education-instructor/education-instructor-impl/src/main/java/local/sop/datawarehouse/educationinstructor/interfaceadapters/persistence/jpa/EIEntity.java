package local.sop.datawarehouse.educationinstructor.interfaceadapters.persistence.jpa;

import java.util.Map;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "education_instructor")
public class EIEntity {
	@EmbeddedId
	private EIId id;
	
	@Version
    private Long version;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected EIEntity() { } // JPA requires a default constructor

	public EIEntity(EIId id, Boolean active) {
		this.id = id;
		this.active = active;
		this.createdAt = LocalDateTime.now();
	}

	/* wither methods */
	public EIEntity withActive(Boolean active) {
		this.active = active;
		return this;
	}

	// Getter methods */
	public EIId getId() {
		return id;
	}

	public boolean isActive() {
		return active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public static Builder builder() {
		return new Builder();
	}

	/* Builder factory inner class */
	public static class Builder {
		private EIId id;
		private Boolean active;

		public Builder id(EIId id) {
			this.id = id;
			return this;
		}

		public Builder active(Boolean active) {
			this.active = active;
			return this;
		}

		public EIEntity build() {

				/* Sanity checks */
                if (id == null) {
                    throw new ValidationException("key.required", Map.of("field", "id"));
                }

                /* preconditions
                * messageRef and personRef are validated in their respective repository adapters, so no need to validate here.
                */

                if (active == null) {
                    throw new ValidationException("educationinstructor.active.required", Map.of("field", "active" ));
				}
					
			return new EIEntity(id, active);
		}
	}


}

package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "logins")
public class LoginEntity {
	@Id
	private UUID id;

	@Version
	private Long version;

	@Column(name = "person_ref", nullable = false)
	private UUID personRef;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "password_hash", nullable = false)
	private String password;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private LoginStatus status;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAtTimestamp;

	@OneToMany(mappedBy = "login", fetch = FetchType.LAZY,
           cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SessionEntity> sessions = new ArrayList<>();


	protected LoginEntity() {} // Required by JPA

	private LoginEntity(UUID id, UUID personRef, String username, String password, LoginStatus status, LocalDateTime createdAtTimestamp, List<SessionEntity> sessions) {
		this.id = id;
		this.personRef = personRef;
		this.username = username;
		this.password = password;
		this.status = status;
		this.createdAtTimestamp = createdAtTimestamp != null ? createdAtTimestamp : LocalDateTime.now();
		this.sessions = sessions != null ? sessions : new ArrayList<>();
	}

	public LoginEntity withPersonRef(UUID personRef) {
		return new LoginEntity(this.id, personRef, this.username, this.password, this.status, this.createdAtTimestamp, this.sessions);
	}

	public LoginEntity withUsername(String username) {
		return new LoginEntity(this.id, this.personRef, username, this.password, this.status, this.createdAtTimestamp, this.sessions);
	}

	public LoginEntity withPassword(String password) {
		return new LoginEntity(this.id, this.personRef, this.username, password, this.status, this.createdAtTimestamp, this.sessions);
	}

	public LoginEntity withStatus(LoginStatus status) {
		return new LoginEntity(this.id, this.personRef, this.username, this.password, status, this.createdAtTimestamp, this.sessions);
	}

	public UUID getId() { return this.id; }
	public UUID getPersonRef() { return this.personRef; }
	public String getUsername() { return this.username; }
	public String getPassword() { return this.password; }
	public LoginStatus getStatus() { return this.status; }
	public LocalDateTime getCreatedAtTimestamp() { return this.createdAtTimestamp; }
	public List<SessionEntity> getSessions() { return sessions; }

	public static Builder builder() { return new Builder(); }

	public static class Builder {
		private UUID id;
		private UUID personRef;
		private String username;
		private String password;
		private LoginStatus status;
		private LocalDateTime createdAtTimestamp;
		private List<SessionEntity> sessions;

		public Builder id(UUID id) {
			if(id == null) throw new ValidationException("login.id.invalid", Map.of("field", "id"));
			this.id = id;
			return this;
		}

		public Builder personRef(UUID personRef) {
			if(personRef == null) throw new ValidationException("login.personref.invalid", Map.of("field", "personRef"));
			this.personRef = personRef;
			return this;
		}

		public Builder username(String username) {
			if(username == null) throw new ValidationException("login.username.invalid", Map.of("field", "username"));
			this.username = username;
			return this;
		}

		public Builder password(String password) {
			if(password == null) throw new ValidationException("login.password.invalid", Map.of("field", "password"));
			this.password = password;
			return this;
		}

		public Builder status(LoginStatus status) {
			if(status == null) throw new ValidationException("login.status.invalid", Map.of("field", "status"));
			this.status = status;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			this.createdAtTimestamp = createdAt;
			return this;
		}

		public Builder sessions(List<SessionEntity> sessions) {
			this.sessions = sessions;
			return this;
		}

		public LoginEntity build() {
			return new LoginEntity(this.id, this.personRef, this.username, this.password, this.status, this.createdAtTimestamp, this.sessions);
		}
	}

}

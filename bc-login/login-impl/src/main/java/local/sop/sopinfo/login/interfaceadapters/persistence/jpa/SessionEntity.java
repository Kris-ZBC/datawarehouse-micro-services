package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "login_sessions")
public class SessionEntity {
	@Id
	private UUID id;

	@Version
	private Long version;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "login_id", nullable = false)
	private LoginEntity login;

	@Column(name = "session_token", unique = true, nullable = false)
	private String sessionToken;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	protected SessionEntity() {} // Required by JPA

	public SessionEntity(UUID id, LoginEntity login, String sessionToken, LocalDateTime createdAt, LocalDateTime expiresAt) {
		this.id = id;
		this.login = login;
		this.sessionToken = sessionToken;
		this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
		this.expiresAt = expiresAt;
	}

	public SessionEntity withLogin(LoginEntity login) {
		return new SessionEntity(this.id, login, this.sessionToken, this.createdAt, this.expiresAt);
	}

	public SessionEntity withSessionToken(String sessionToken) {
		return new SessionEntity(this.id, this.login, sessionToken, this.createdAt, this.expiresAt);
	}

	public SessionEntity withExpiresAt(LocalDateTime expiresAt) {
		return new SessionEntity(this.id, this.login, this.sessionToken, this.createdAt, expiresAt);
	}
	// Getter — expose the UUID for domain mapping
	public UUID getId() { return id; }
	// Also keep the LoginEntity getter for JPA use
	public LoginEntity getLogin() { return login; }
	public String getSessionToken() { return sessionToken; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getExpiresAt() { return expiresAt; }
	
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(this.expiresAt);
	}

	public static Builder builder() { return new Builder(); }

	public static class Builder {
		private UUID id;
		private LoginEntity login;
		private String sessionToken;
		private LocalDateTime createdAt;
		private LocalDateTime expiresAt;

		public Builder id(UUID id) {
			if(id == null) throw new ValidationException("session.id.invalid", Map.of("field", "id"));
			this.id = id;
			return this;
		}

		public Builder login(LoginEntity login) {
			if (login == null) throw new ValidationException(
				"session.loginid.invalid", Map.of("field", "loginId"));
			this.login = login;
			return this;
		}

		public Builder sessionToken(String sessionToken) {
			if(sessionToken == null || sessionToken.isBlank()) throw new ValidationException("session.token.invalid", Map.of("field", "sessionToken"));
			this.sessionToken = sessionToken;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder expiresAt(LocalDateTime expiresAt) {
			if(expiresAt == null) throw new ValidationException("session.expiresattimestamp.invalid", Map.of("field", "expiresAt"));
			this.expiresAt = expiresAt;
			return this;
		}

		public SessionEntity build() {
			return new SessionEntity(this.id, this.login, this.sessionToken, this.createdAt, this.expiresAt);
		}
	}
}

package local.sop.datawarehouse.login.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.ExpiresAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionId;
import local.sop.datawarehouse.login.domain.model.valueobjects.SessionToken;
import local.sop.common.libs.sharedkernel.enums.UserRole;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class Session {
	private final SessionId id;
	private final SessionToken token;
	private final CreatedAtTimestamp createdAt;
	private final ExpiresAtTimestamp expiresAt;
	private final Login login; // Optional reference to the Login aggregate for domain logic
	// CHANGED: resolved once by login-saga at session-creation time
	// (checking bc-instructor/bc-apprentice) and stored here — NOT
	// looked up fresh on every validateSession() call. That keeps
	// validateSession() a cheap single-BC read; the cost of a role
	// change taking effect is bounded by session lifetime (8h) rather
	// than being paid on every request.
	private final UserRole role;

	private Session(SessionId id, SessionToken token, CreatedAtTimestamp createdAt, ExpiresAtTimestamp expiresAt, Login login, UserRole role) {
		this.id = id;
		this.token = token;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.login = login;
		this.role = role;
	}

	public Session withLogin(Login login) {
		return new Session(this.id, this.token, this.createdAt, this.expiresAt, login, this.role);
	}

	public Session withSessionToken(SessionToken token) {
		return new Session(this.id, token, this.createdAt, this.expiresAt, this.login, this.role);
	}

	public Session withCreatedAt(CreatedAtTimestamp createdAt) {
		return new Session(this.id, this.token, createdAt, this.expiresAt, this.login, this.role);
	}

	public Session withExpiresAt(ExpiresAtTimestamp expiresAt) {
		return new Session(this.id, this.token, this.createdAt, expiresAt, this.login, this.role);
	}

	public Session withRole(UserRole role) {
		return new Session(this.id, this.token, this.createdAt, this.expiresAt, this.login, role);
	}

	public SessionId getId() { return this.id; }
	public Login getLogin() { return this.login;}
	public SessionToken getToken() { return this.token; }
	public CreatedAtTimestamp getCreatedAt() { return this.createdAt; }
	public ExpiresAtTimestamp getExpiresAt() { return this.expiresAt; }
	public UserRole getRole() { return this.role; }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private SessionId id;
		private SessionToken token;
		private CreatedAtTimestamp createdAt;
		private ExpiresAtTimestamp expiresAt;
		private Login login;
		private UserRole role;

		public Builder id(SessionId id) { this.id = id; return this; }
		public Builder sessionToken(SessionToken token) { this.token = token; return this; }
		public Builder createdAt(CreatedAtTimestamp createdAt) { this.createdAt = createdAt; return this; }
		public Builder expiresAt(ExpiresAtTimestamp expiresAt) { this.expiresAt = expiresAt; return this; }
		public Builder login(Login login) { this.login = login; return this; }
		public Builder role(UserRole role) { this.role = role; return this; }

		public Session build() {
			if(id == null) this.id = SessionId.newId();
			if(login == null) throw new ValidationException("session.login.invalid", Map.of("field", "login"));
			if(token == null) throw new ValidationException("session.token.invalid", Map.of("field", "token"));
			if(createdAt == null) this.createdAt = new CreatedAtTimestamp(LocalDateTime.now());
			if(expiresAt == null) throw new ValidationException("session.expiresattimestamp.invalid", Map.of("field", "expiresAt"));
			if(role == null) throw new ValidationException("session.role.invalid", Map.of("field", "role"));
			return new Session(id, token, createdAt, expiresAt, login, role);
		}
	}
}
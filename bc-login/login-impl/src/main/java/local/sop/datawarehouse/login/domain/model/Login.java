package local.sop.datawarehouse.login.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.datawarehouse.login.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;

public class Login {
	private final LoginId id;
	private final PersonRef personRef;
	private final Username username;
	private final HashedPassword password;
	private final LoginStatus status;
	private final CreatedAtTimestamp createdAt;

	private Login(LoginId id, PersonRef personRef, Username username, HashedPassword password, LoginStatus status, CreatedAtTimestamp createdAt) {
		this.id = id;
		this.personRef = personRef;
		this.username = username;
		this.password = password;
		this.status = status;
		this.createdAt = createdAt;
	}

	public Login withPersonRef(PersonRef personRef) {
		return new Login(this.id, personRef, this.username, this.password, this.status, this.createdAt);
	}

	public Login withUsername(Username username) {
		return new Login(this.id, this.personRef, username, this.password, this.status, this.createdAt);
	}

	public Login withPassword(HashedPassword password) {
		return new Login(this.id, this.personRef, this.username, password, this.status, this.createdAt);
	}

	public Login withStatus(LoginStatus status) {
		return new Login(this.id, this.personRef, this.username, this.password, status, this.createdAt);
	}

	// Getters
	public LoginId getId() { return this.id; }
	public PersonRef getPersonRef() { return this.personRef; }
	public Username getUsername() { return this.username; }
	public HashedPassword getPassword() { return this.password; }
	public LoginStatus getStatus() { return this.status; }
	public CreatedAtTimestamp getCreatedAt() { return this.createdAt; }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private LoginId id;
		private PersonRef personRef;
		private Username username;
		private HashedPassword password;
		private LoginStatus status;
		private CreatedAtTimestamp createdAt;


		public Builder id(LoginId id) { this.id = id; return this; }
		public Builder personRef(PersonRef personRef) { this.personRef = personRef; return this; }
		public Builder username(Username username) { this.username = username; return this; }
		public Builder password(HashedPassword password) { this.password = password; return this; }
		public Builder status(LoginStatus status) { this.status = status; return this;}
		public Builder createdAt(CreatedAtTimestamp createdAt) { this.createdAt = createdAt; return this; }

		public Login build() {
			if(id == null) id = LoginId.newId();
			if(personRef == null) throw new ValidationException("login.personref.invalid", Map.of("field", "personRef"));
			if(username == null) throw new ValidationException("login.username.invalid", Map.of("field", "username"));
			if(password == null) throw new ValidationException("login.password.invalid", Map.of("field", "password"));
			if(status == null) throw new ValidationException("login.status.invalid", Map.of("field", "status"));
			if(createdAt == null) createdAt = new CreatedAtTimestamp(LocalDateTime.now());
			return new Login(id, personRef, username, password, status, createdAt);
		}
	}
}

package local.sop.sopinfo.notification.interfaceadapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name = "notification")
public class NotificationEntity {
	@Id
	private UUID id;

	@Version
	private Long version;

	@Column(name = "message_ref", nullable = false)
	private UUID messageRef;

	@Column(name = "seen", nullable = false)
	private boolean seen;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected NotificationEntity() {}

	private NotificationEntity(UUID id, UUID messageRef, boolean seen) {
		this.id = id;
		this.messageRef = messageRef;
		this.seen = seen;
		this.createdAt = LocalDateTime.now();
	}

	public NotificationEntity withMessageRef(UUID messageRef) {
		return new NotificationEntity(this.id, messageRef, this.seen);
	}

	public NotificationEntity withSeen(boolean seen) {
		return new NotificationEntity(this.id, this.messageRef, seen);
	}

	public void setSeen(boolean seen) { this.seen = seen; }

	public UUID getId() { return this.id; }
	public UUID getMessageRef() { return this.messageRef; }
	public boolean getSeen() { return this.seen; }
	public LocalDateTime getCreatedAtTimestamp() { return this.createdAt; }

	public static Builder builder() { return new Builder(); }

	public static class Builder {
		private UUID id;
		private UUID messageRef;
		private boolean seen;

		public Builder id(UUID id) {
			if (id == null) throw new ValidationException("notification.id.invalid", Map.of("field", "id"));
			this.id = id;
			return this;
		}

		public Builder messageRef(UUID messageRef) {
			if (messageRef == null) throw new ValidationException("notification.messageref.invalid", Map.of("field", "messageRef"));
			this.messageRef = messageRef;
			return this;
		}

		public NotificationEntity build() {
			return new NotificationEntity(this.id, this.messageRef, this.seen);
		}
	}
}

package local.sop.sopinfo.notification.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.notification.domain.model.valueobjects.*;

public class Notification {
	private final DomainId id;
	private final DomainId messageRef;
	private final boolean seen;
	private final CreatedAtTimestamp createdAt;

	private Notification(DomainId id, DomainId messageRef,	boolean seen, CreatedAtTimestamp createdAt) {
		this.id = id;
		this.messageRef = messageRef;
		this.seen = seen;
		this.createdAt = createdAt;
	}

	public Notification withMessageRef(DomainId messageRef) {
		return new Notification(this.id, messageRef, this.seen, this.createdAt);
	}

	public Notification withSeen(boolean seen) {
		return new Notification(this.id, this.messageRef, seen, this.createdAt);
	}

	// Getters
	public DomainId getId() { return this.id; }
	public DomainId getMessageRef() { return this.messageRef; }
	public boolean getSeen() { return this.seen; }
	public CreatedAtTimestamp getCreatedAt() { return this.createdAt; }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private DomainId id;
		private MessageRef messageRef;
		private boolean seen;
		private CreatedAtTimestamp createdAt;

		public Builder id(DomainId id) { this.id = id; return this; }
		public Builder messageRef(MessageRef messageRef) { this.messageRef = messageRef; return this; }
		public Builder seen(boolean seen) { this.seen = seen; return this; }
		public Builder createdAt(CreatedAtTimestamp createdAt) { this.createdAt = createdAt; return this; }

		public Notification build() {
			if(id == null) id = NotificationId.newId();
			if(messageRef == null) throw new ValidationException("notification.messageref.invalid", Map.of("field", "messageRef"));
			if(createdAt == null) createdAt = new CreatedAtTimestamp(LocalDateTime.now());
			return new Notification(id, messageRef, seen, createdAt);
		}
	}
}

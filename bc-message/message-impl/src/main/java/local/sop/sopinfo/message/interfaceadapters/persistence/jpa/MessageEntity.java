package local.sop.sopinfo.message.interfaceadapters.persistence.jpa;

import java.time.OffsetDateTime;

import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "message")
public class MessageEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "date_time_sent", nullable = false)
    private OffsetDateTime dateTimeSent;

    @Column(name = "message_text", nullable = false, length = 4000)
    private String message;

    @Column(name = "sender_person_ref", nullable = false, columnDefinition = "BINARY(16)")
    private UUID senderPersonRef;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "message_recipient",
        joinColumns = @JoinColumn(name = "message_id")
    )

    @Version
    @Column(name = "version", nullable = false)
    private long version;

	protected MessageEntity(){};

	private MessageEntity(UUID id, OffsetDateTime dateTimeSent, String message, UUID senderPersonRef){
		this.id = id;
		this.dateTimeSent = dateTimeSent;
		this.message = message;
		this.senderPersonRef = senderPersonRef;
	}

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getDateTimeSent() {
        return dateTimeSent;
    }

    public String getMessage() {
        return message;
    }

    public UUID getSenderPersonRef() {
        return senderPersonRef;
    }

    public long getVersion() {
        return version;
    }


	public MessageEntity withId(UUID id) {
        return new MessageEntity(id, this.dateTimeSent, this.message, this.senderPersonRef);
    }
	public MessageEntity withDateTimeSent(OffsetDateTime dateTimeSent) {
        return new MessageEntity(this.id, dateTimeSent, this.message, this.senderPersonRef);
    }
	public MessageEntity withMessage(String message) {
        return new MessageEntity(this.id, this.dateTimeSent, message, this.senderPersonRef);
    }
    public MessageEntity withSenderPersonRef(UUID senderPersonRef) {
        return new MessageEntity(this.id, this.dateTimeSent, this.message, senderPersonRef);
    }

}
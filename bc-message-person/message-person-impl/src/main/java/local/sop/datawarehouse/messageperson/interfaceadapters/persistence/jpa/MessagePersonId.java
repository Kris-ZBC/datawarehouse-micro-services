package local.sop.datawarehouse.messageperson.interfaceadapters.persistence.jpa;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MessagePersonId implements Serializable {
    @Column(name = "message_ref", nullable = false)
    private UUID messageRef;

    @Column(name = "person_ref", nullable = false)
    private UUID personRef;

    public MessagePersonId(UUID messageRef, UUID personRef) {
        this.messageRef = messageRef;
        this.personRef = personRef;
    }
    
    protected MessagePersonId() {} // JPA requires a default constructor

    public UUID getMessageRef() {
        return messageRef;
    }

    public UUID getPersonRef() {
        return personRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessagePersonId that = (MessagePersonId) o;

        if (!messageRef.equals(that.messageRef)) return false;
        return personRef.equals(that.personRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageRef, personRef);
    }
}

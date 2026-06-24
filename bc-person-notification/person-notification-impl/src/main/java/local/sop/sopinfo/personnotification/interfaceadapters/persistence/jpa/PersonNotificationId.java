package local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa;

import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


@Embeddable
public class PersonNotificationId implements Serializable {
    @Column(name = "person_ref", nullable = false)
    private UUID personRef;

    @Column(name = "notification_ref", nullable = false)
    private UUID notificationRef;
    
    protected PersonNotificationId() {}
    
    public PersonNotificationId(UUID personRef, UUID notificationRef) {
        this.personRef = personRef;
        this.notificationRef = notificationRef;
    }

    public UUID getPersonRef() {
        return personRef;
    }

    public UUID getNotificationRef() {
        return notificationRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonNotificationId that = (PersonNotificationId) o;

        if (!personRef.equals(that.personRef)) return false;
        return notificationRef.equals(that.notificationRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personRef, notificationRef);
    }
    
}

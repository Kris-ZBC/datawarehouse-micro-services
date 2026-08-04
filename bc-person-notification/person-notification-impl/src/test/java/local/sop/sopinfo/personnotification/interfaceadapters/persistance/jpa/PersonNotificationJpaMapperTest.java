package local.sop.sopinfo.personnotification.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationJpaMapper;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationEntity;
import local.sop.sopinfo.personnotification.interfaceadapters.persistence.jpa.PersonNotificationId;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

class PersonNotificationJpaMapperTest {

    @Test
    void toEntity_and_toDomain_preserveImportantFields() {
        PersonNotificationJpaMapper mapper = new PersonNotificationJpaMapper();

        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        PersonNotification domain = new PersonNotification.Builder()
                .id(new CompositeKey(messageRef, personRef))
                .active(true)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();

        // Domain → Entity
        PersonNotificationEntity entity = mapper.toEntity(domain);

        // Entity → Domain
        PersonNotification mappedBack = mapper.toDomain(entity);

        assertEquals(messageRef, mappedBack.getId().key1());
        assertEquals(personRef, mappedBack.getId().key2());
        assertTrue(mappedBack.isActive());

        // createdAt should be preserved and not null
        assertNotNull(mappedBack.getCreatedAt());
        assertEquals(entity.getCreatedAt(), mappedBack.getCreatedAt().value());
    }

    @Test
    void toDomain_mapsEntityCorrectly() {
        PersonNotificationJpaMapper mapper = new PersonNotificationJpaMapper();

        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        PersonNotificationEntity entity = PersonNotificationEntity.builder()
                .id(new PersonNotificationId(messageRef, personRef))
                .active(true)
                .build();

        PersonNotification domain = mapper.toDomain(entity);

        assertEquals(messageRef, domain.getId().key1());
        assertEquals(personRef, domain.getId().key2());
        assertTrue(domain.isActive());
        assertNotNull(domain.getCreatedAt());
    }

    @Test
    void updateIntoEntity_updatesActiveField() {
        PersonNotificationJpaMapper mapper = new PersonNotificationJpaMapper();

        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        PersonNotification domain = new PersonNotification.Builder()
                .id(new CompositeKey(messageRef, personRef))
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        PersonNotificationEntity entity = PersonNotificationEntity.builder()
                .id(new PersonNotificationId(messageRef, personRef))
                .active(true)
                .build();

        mapper.updateIntoEntity(domain, entity);

        assertFalse(entity.isActive());
    }
}

package local.sop.sopinfo.messageperson.interfaceadapters.persistance.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.messageperson.domain.model.MessagePerson;
import local.sop.sopinfo.messageperson.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.sopinfo.messageperson.interfaceadapters.persistence.jpa.MessagePersonDomainJpaMapper;
import local.sop.sopinfo.messageperson.interfaceadapters.persistence.jpa.MessagePersonEntity;
import local.sop.sopinfo.messageperson.interfaceadapters.persistence.jpa.MessagePersonId;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

class MessagePersonDomainJpaMapperTest {

    @Test
    void toEntity_and_toDomain_preserveImportantFields() {
        MessagePersonDomainJpaMapper mapper = new MessagePersonDomainJpaMapper();

        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        MessagePerson domain = new MessagePerson.Builder()
                .id(new CompositeKey(messageRef, personRef))
                .active(true)
                .createdAt(new CreatedAtTimestamp(createdAt))
                .build();

        // Domain → Entity
        MessagePersonEntity entity = mapper.toEntity(domain);

        // Entity → Domain
        MessagePerson mappedBack = mapper.toDomain(entity);

        assertEquals(messageRef, mappedBack.getId().key1());
        assertEquals(personRef, mappedBack.getId().key2());
        assertTrue(mappedBack.isActive());

        // createdAt should be preserved and not null
        assertNotNull(mappedBack.getCreatedAt());
        assertEquals(entity.getCreatedAt(), mappedBack.getCreatedAt().value());
    }

    @Test
    void toDomain_mapsEntityCorrectly() {
        MessagePersonDomainJpaMapper mapper = new MessagePersonDomainJpaMapper();

        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        MessagePersonEntity entity = MessagePersonEntity.builder()
                .id(new MessagePersonId(messageRef, personRef))
                .active(true)
                .build();

        MessagePerson domain = mapper.toDomain(entity);

        assertEquals(messageRef, domain.getId().key1());
        assertEquals(personRef, domain.getId().key2());
        assertTrue(domain.isActive());
        assertNotNull(domain.getCreatedAt());
    }

    @Test
    void updateIntoEntity_updatesActiveField() {
        MessagePersonDomainJpaMapper mapper = new MessagePersonDomainJpaMapper();

        UUID messageRef = UUID.randomUUID();
        UUID personRef = UUID.randomUUID();

        MessagePerson domain = new MessagePerson.Builder()
                .id(new CompositeKey(messageRef, personRef))
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        MessagePersonEntity entity = MessagePersonEntity.builder()
                .id(new MessagePersonId(messageRef, personRef))
                .active(true)
                .build();

        mapper.updateIntoEntity(domain, entity);

        assertFalse(entity.isActive());
    }
}

package local.sop.sopinfo.auditlog.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class AuditlogEntityTest {

    private UUID id = UUID.randomUUID();
    private UUID actorRef = UUID.randomUUID();
    private Instant now = Instant.now();

    private AuditlogEntity createValidEntity() {
        return AuditlogEntity.builder()
                .id(id)
                .actorType(ActorType.USER)
                .actorRef(actorRef)
                .originSystem("system")
                .originService("service")
                .originComponent("component")
                .severity(Severity.INFO)
                .data("data")
                .description("desc")
                .timestamp(now)
                .build();
    }

    /** -------------------------
     *  Builder happy path
     *  ------------------------- */
    @Test
    void builder_shouldCreateEntity() {
        AuditlogEntity entity = createValidEntity();

        assertEquals(id, entity.getId());
        assertEquals(ActorType.USER, entity.getActorType());
        assertEquals(actorRef, entity.getActorRef());
        assertEquals("system", entity.getOriginSystem());
        assertEquals("service", entity.getOriginService());
        assertEquals("component", entity.getOriginComponent());
        assertEquals(Severity.INFO, entity.getSeverity());
        assertEquals("data", entity.getData());
        assertEquals("desc", entity.getDescription());
        assertEquals(now, entity.getTimestamp());
    }

    /** -------------------------
     *  Withers
     *  ------------------------- */
    @Test
    void withers_shouldReturnNewModifiedInstances() {
        AuditlogEntity entity = createValidEntity();

        AuditlogEntity updated = entity
                .withActorType(ActorType.SYSTEM)
                .withActorRef(UUID.randomUUID())
                .withOriginSystem("newSystem")
                .withOriginService("newService")
                .withOriginComponent("newComponent")
                .withSeverity(Severity.DEBUG)
                .withData("newData")
                .withDescription("newDesc")
                .withTimestamp(Instant.now());

        assertNotSame(entity, updated);

        assertEquals(ActorType.SYSTEM, updated.getActorType());
        assertEquals("newSystem", updated.getOriginSystem());
        assertEquals("newService", updated.getOriginService());
        assertEquals("newComponent", updated.getOriginComponent());
        assertEquals(Severity.DEBUG, updated.getSeverity());
        assertEquals("newData", updated.getData());
        assertEquals("newDesc", updated.getDescription());
    }

    /** -------------------------
     *  withId (explicit test)
     *  ------------------------- */
    @Test
    void withId_shouldReturnNewInstance() {
        AuditlogEntity entity = createValidEntity();
        UUID newId = UUID.randomUUID();

        AuditlogEntity updated = entity.withId(newId);

        assertEquals(newId, updated.getId());
        assertNotSame(entity, updated);
    }

    /** -------------------------
     *  Builder validation tests
     *  ------------------------- */

    @Test
    void builder_shouldThrowWhenIdNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().id(null));

        assertNotNull(ex);
    }

    @Test
    void builder_shouldThrowWhenActorTypeNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().actorType(null));
    }

    @Test
    void builder_shouldThrowWhenActorRefNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().actorRef(null));
    }

    @Test
    void builder_shouldThrowWhenOriginSystemNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().originSystem(null));
    }

    @Test
    void builder_shouldThrowWhenOriginServiceNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().originService(null));
    }

    @Test
    void builder_shouldThrowWhenOriginComponentNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().originComponent(null));
    }

    @Test
    void builder_shouldThrowWhenSeverityNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().severity(null));
    }

    @Test
    void builder_shouldThrowWhenDataNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().data(null));
    }

    @Test
    void builder_shouldThrowWhenTimestampNull() {
        assertThrows(ValidationException.class,
                () -> AuditlogEntity.builder().timestamp(null));
    }

    /** -------------------------
     *  Optional field (description)
     *  ------------------------- */
    @Test
    void builder_shouldAllowNullDescription() {
        AuditlogEntity entity = AuditlogEntity.builder()
                .id(id)
                .actorType(ActorType.USER)
                .actorRef(actorRef)
                .originSystem("system")
                .originService("service")
                .originComponent("component")
                .severity(Severity.INFO)
                .data("data")
                .timestamp(now)
                .build();

        assertNull(entity.getDescription());
    }
}
package local.sop.sopinfo.auditlog.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.auditlog.domain.model.Log;
import local.sop.sopinfo.auditlog.domain.model.valueobjects.*;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;

class AuditlogJpaMapperTest {
    @Test
    void toEntity_and_toDomain_roundTrip_withDescription() {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();
        Instant ts = Instant.now();

        Log domain = Log.builder()
                .id(new LogId(id))
                .actorRef(new ActorRef(actorRef))
                .actorType(ActorType.USER)
                .severity(Severity.INFO)
                .originSystem(new OriginSystem("sys"))
                .originService(new OriginService("svc"))
                .originComponent(new OriginComponent("cmp"))
                .data(new Data("{\"a\":1}"))
                .description(new Description("desc"))
                .timestamp(new LogTimestamp(ts))
                .build();

        AuditlogEntity entity = AuditlogJpaMapper.toEntity(domain);
        Log mappedBack = AuditlogJpaMapper.toDomain(entity);

        assertEquals(id, mappedBack.getId().value());
        assertEquals(actorRef, mappedBack.getActorRef().value());
        assertEquals(ActorType.USER, mappedBack.getActorType());
        assertEquals(Severity.INFO, mappedBack.getSeverity());
        assertEquals("sys", mappedBack.getOriginSystem().value());
        assertEquals("svc", mappedBack.getOriginService().value());
        assertEquals("cmp", mappedBack.getOriginComponent().value());
        assertEquals("{\"a\":1}", mappedBack.getData().json());
        assertNotNull(mappedBack.getDescription());
        assertEquals("desc", mappedBack.getDescription().value());
        assertEquals(ts, mappedBack.getTimestamp().value());
    }

    @Test
    void toDomain_shouldKeepDescriptionNull_whenEntityHasNull() {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();
        Instant ts = Instant.now();

        AuditlogEntity entity = new AuditlogEntity.Builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(ActorType.USER)
                .severity(Severity.INFO)
                .originSystem("sys")
                .originService("svc")
                .originComponent("cmp")
                .data("{\"a\":1}")
                .description(null) // rammer ternary i mapperen
                .timestamp(ts)
                .build();

        Log domain = AuditlogJpaMapper.toDomain(entity);

        assertNull(domain.getDescription());
    }

    @Test
    void toEntity_shouldSetDescriptionNull_whenDomainDescriptionIsNull() {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();
        Instant ts = Instant.now();

        Log domain = Log.builder()
                .id(new LogId(id))
                .actorRef(new ActorRef(actorRef))
                .actorType(ActorType.USER)
                .severity(Severity.INFO)
                .originSystem(new OriginSystem("sys"))
                .originService(new OriginService("svc"))
                .originComponent(new OriginComponent("cmp"))
                .data(new Data("{\"k\":\"v\"}"))
                .description(null) // rammer ternary i mapperen
                .timestamp(new LogTimestamp(ts))
                .build();

        AuditlogEntity entity = AuditlogJpaMapper.toEntity(domain);

        assertNull(entity.getDescription());
    }

    @Test
    void privateConstructor_shouldBeInvokable_forCoverage() throws Exception {
        var ctor = AuditlogJpaMapper.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object instance = ctor.newInstance();
        assertNotNull(instance);
    }
}
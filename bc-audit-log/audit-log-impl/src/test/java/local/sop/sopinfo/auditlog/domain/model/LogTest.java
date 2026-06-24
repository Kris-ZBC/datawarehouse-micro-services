package local.sop.sopinfo.auditlog.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.auditlog.domain.model.valueobjects.*;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

class LogTest {

    private final LogId id = LogId.newId();
    private final ActorRef actorRef = new ActorRef(UUID.randomUUID());
    private final ActorType actorType = ActorType.USER;
    private final Severity severity = Severity.INFO;
    private final OriginSystem originSystem = new OriginSystem("sys");
    private final OriginService originService = new OriginService("svc");
    private final OriginComponent originComponent = new OriginComponent("comp");
    private final Data data = new Data("{\"key\":\"value\"}");
    private final Description description = new Description("desc");
    private final LogTimestamp timestamp = LogTimestamp.now();

    @Test
    void builder_shouldBuildSuccessfully_whenAllRequiredFieldsAreProvided() {
        Log log = Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build();

        assertEquals(id, log.getId());
        assertEquals(actorRef, log.getActorRef());
        assertEquals(actorType, log.getActorType());
        assertEquals(severity, log.getSeverity());
        assertEquals(originSystem, log.getOriginSystem());
        assertEquals(originService, log.getOriginService());
        assertEquals(originComponent, log.getOriginComponent());
        assertEquals(data, log.getData());
        assertEquals(description, log.getDescription());
        assertEquals(timestamp, log.getTimestamp());
    }

    @Test
    void builder_shouldGenerateId_whenIdIsNotProvided() {
        Log log = Log.builder()
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build();

        assertNotNull(log.getId());
        assertNotNull(log.getId().value());
    }

    @Test
    void builder_shouldAllowNullDescription() {
        Log log = Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .timestamp(timestamp)
                .build();

        assertEquals(id, log.getId());
        assertNull(log.getDescription());
    }

    @Test
    void builder_shouldThrow_whenActorRefIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenActorTypeIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenSeverityIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenOriginSystemIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenOriginServiceIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenOriginComponentIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .data(data)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenDataIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .description(description)
                .timestamp(timestamp)
                .build());
    }

    @Test
    void builder_shouldThrow_whenTimestampIsNull() {
        assertThrows(ValidationException.class, () -> Log.builder()
                .id(id)
                .actorRef(actorRef)
                .actorType(actorType)
                .severity(severity)
                .originSystem(originSystem)
                .originService(originService)
                .originComponent(originComponent)
                .data(data)
                .description(description)
                .build());
    }
}
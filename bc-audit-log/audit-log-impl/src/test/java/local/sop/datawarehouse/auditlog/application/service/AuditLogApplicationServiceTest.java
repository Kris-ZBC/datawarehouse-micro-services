package local.sop.datawarehouse.auditlog.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.auditlog.application.api.dto.AuditlogResponse;
import local.sop.datawarehouse.auditlog.application.api.dto.CompensateAuditlogCmd;
import local.sop.datawarehouse.auditlog.application.api.dto.CreateAuditlogCmd;
import local.sop.datawarehouse.auditlog.application.api.dto.CreatedAuditlogResponse;
import local.sop.datawarehouse.auditlog.domain.model.Log;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.ActorRef;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.Data;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.Description;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.LogId;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.LogTimestamp;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.OriginComponent;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.OriginService;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.OriginSystem;
import local.sop.datawarehouse.auditlog.domain.ports.out.AuditlogRepositoryPort;
import local.sop.datawarehouse.auditlog.domain.service.AuditLogDomain;

@ExtendWith(MockitoExtension.class)
class AuditLogApplicationServiceTest {

    @Mock
    private AuditLogDomain auditLogDomain;

    @Mock
    private AuditlogRepositoryPort repository;

    @InjectMocks
    private AuditlogApplicationService service;

    @Test
    void createAuditlog_shouldMapCmd_callDomain_andReturnSavedId() {
        UUID actorRef = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();

        CreateAuditlogCmd cmd = new CreateAuditlogCmd(
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"created\"}",
                "created from test"
        );

        Log created = log(
                createdId,
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"created\"}",
                "created from test",
                Instant.parse("2026-03-18T12:00:00Z")
        );

        Log saved = log(
                savedId,
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"created\"}",
                "created from test",
                Instant.parse("2026-03-18T12:00:01Z")
        );

        when(auditLogDomain.createLog(any(Log.class))).thenReturn(created);
        when(repository.save(created)).thenReturn(saved);

        CreatedAuditlogResponse response = service.createAuditlog(cmd);

        assertEquals(savedId, response.id());

        ArgumentCaptor<Log> draftCaptor = ArgumentCaptor.forClass(Log.class);
        verify(auditLogDomain).createLog(draftCaptor.capture());
        verify(repository).save(created);

        Log draft = draftCaptor.getValue();
        assertNotNull(draft.getId());
        assertEquals(actorRef, draft.getActorRef().value());
        assertEquals(ActorType.USER, draft.getActorType());
        assertEquals(Severity.INFO, draft.getSeverity());
        assertEquals("audit-system", draft.getOriginSystem().value());
        assertEquals("audit-service", draft.getOriginService().value());
        assertEquals("audit-component", draft.getOriginComponent().value());
        assertEquals("{\"event\":\"created\"}", draft.getData().json());
        assertNotNull(draft.getDescription());
        assertEquals("created from test", draft.getDescription().value());
        assertNotNull(draft.getTimestamp());
    }

    @Test
    void createAuditlog_shouldAllowNullDescription() {
        UUID actorRef = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();

        CreateAuditlogCmd cmd = new CreateAuditlogCmd(
                actorRef,
                ActorType.SYSTEM,
                Severity.DEBUG,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"debug\"}",
                null
        );

        Log created = log(
                UUID.randomUUID(),
                actorRef,
                ActorType.SYSTEM,
                Severity.DEBUG,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"debug\"}",
                null,
                Instant.parse("2026-03-18T12:05:00Z")
        );

        Log saved = log(
                savedId,
                actorRef,
                ActorType.SYSTEM,
                Severity.DEBUG,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"debug\"}",
                null,
                Instant.parse("2026-03-18T12:05:01Z")
        );

        when(auditLogDomain.createLog(any(Log.class))).thenReturn(created);
        when(repository.save(created)).thenReturn(saved);

        CreatedAuditlogResponse response = service.createAuditlog(cmd);

        assertEquals(savedId, response.id());

        ArgumentCaptor<Log> draftCaptor = ArgumentCaptor.forClass(Log.class);
        verify(auditLogDomain).createLog(draftCaptor.capture());

        Log draft = draftCaptor.getValue();
        assertNotNull(draft.getDescription());
        assertNull(draft.getDescription().value());
    }

    @Test
    void findAll_shouldMapAllLogsToResponses() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID actorRef1 = UUID.randomUUID();
        UUID actorRef2 = UUID.randomUUID();

        when(repository.findAll()).thenReturn(List.of(
                log(id1, actorRef1, ActorType.USER, Severity.INFO,
                        "sys1", "svc1", "cmp1", "{\"a\":1}", "desc1",
                        Instant.parse("2026-03-18T10:00:00Z")),
                log(id2, actorRef2, ActorType.SYSTEM, Severity.ERROR,
                        "sys2", "svc2", "cmp2", "{\"a\":2}", null,
                        Instant.parse("2026-03-18T11:00:00Z"))
        ));

        List<AuditlogResponse> responses = service.findAll();

        assertEquals(2, responses.size());

        AuditlogResponse first = responses.get(0);
        assertEquals(id1, first.id());
        assertEquals(actorRef1, first.actorRef());
        assertEquals(ActorType.USER, first.actorType());
        assertEquals(Severity.INFO, first.severity());
        assertEquals("sys1", first.originSystem());
        assertEquals("svc1", first.originService());
        assertEquals("cmp1", first.originComponent());
        assertEquals("{\"a\":1}", first.data());
        assertEquals("desc1", first.description());
        assertEquals(Instant.parse("2026-03-18T10:00:00Z"), first.timestamp());

        AuditlogResponse second = responses.get(1);
        assertEquals(id2, second.id());
        assertEquals(actorRef2, second.actorRef());
        assertEquals(ActorType.SYSTEM, second.actorType());
        assertEquals(Severity.ERROR, second.severity());
        assertEquals("sys2", second.originSystem());
        assertEquals("svc2", second.originService());
        assertEquals("cmp2", second.originComponent());
        assertEquals("{\"a\":2}", second.data());
        assertNull(second.description());
        assertEquals(Instant.parse("2026-03-18T11:00:00Z"), second.timestamp());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenRepositoryReturnsEmpty() {
        when(repository.findAll()).thenReturn(List.of());

        List<AuditlogResponse> responses = service.findAll();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void findBySearchParams_shouldPassFiltersToRepository_andMapResponses() {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();

        Log result = log(
                id,
                actorRef,
                ActorType.USER,
                Severity.WARNING,
                "search-system",
                "search-service",
                "search-component",
                "{\"filter\":true}",
                "matched",
                Instant.parse("2026-03-18T13:00:00Z")
        );

        when(repository.findBySearchParams(
                id,
                actorRef,
                ActorType.USER,
                "search-system",
                "search-service",
                "search-component",
                Severity.WARNING
        )).thenReturn(List.of(result));

        List<AuditlogResponse> responses = service.findBySearchParams(
                id,
                actorRef,
                ActorType.USER,
                Severity.WARNING,
                "search-system",
                "search-service",
                "search-component"
        );

        verify(repository).findBySearchParams(
                id,
                actorRef,
                ActorType.USER,
                "search-system",
                "search-service",
                "search-component",
                Severity.WARNING
        );

        assertEquals(1, responses.size());
        AuditlogResponse response = responses.get(0);
        assertEquals(id, response.id());
        assertEquals(actorRef, response.actorRef());
        assertEquals(ActorType.USER, response.actorType());
        assertEquals(Severity.WARNING, response.severity());
        assertEquals("search-system", response.originSystem());
        assertEquals("search-service", response.originService());
        assertEquals("search-component", response.originComponent());
        assertEquals("{\"filter\":true}", response.data());
        assertEquals("matched", response.description());
        assertEquals(Instant.parse("2026-03-18T13:00:00Z"), response.timestamp());
    }

    @Test
    void findBySearchParams_shouldReturnEmptyList_whenRepositoryReturnsEmpty() {
        when(repository.findBySearchParams(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(List.of());

        List<AuditlogResponse> responses = service.findBySearchParams(
                null, null, null, null, null, null, null
        );

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    private static Log log(
            UUID id,
            UUID actorRef,
            ActorType actorType,
            Severity severity,
            String originSystem,
            String originService,
            String originComponent,
            String data,
            String description,
            Instant timestamp
    ) {
        return Log.builder()
                .id(new LogId(id))
                .actorRef(new ActorRef(actorRef))
                .actorType(actorType)
                .severity(severity)
                .originSystem(new OriginSystem(originSystem))
                .originService(new OriginService(originService))
                .originComponent(new OriginComponent(originComponent))
                .data(new Data(data))
                .description(new Description(description))
                .timestamp(new LogTimestamp(timestamp))
                .build();
    }

    @Test
    void toResponse_shouldHandleNullDescriptionObject() {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();

        // Create a Log with a NULL Description OBJECT (not just null value)
        Log log = Log.builder()
                .id(new LogId(id))
                .actorRef(new ActorRef(actorRef))
                .actorType(ActorType.USER)
                .severity(Severity.INFO)
                .originSystem(new OriginSystem("sys"))
                .originService(new OriginService("svc"))
                .originComponent(new OriginComponent("cmp"))
                .data(new Data("{\"a\":1}"))
                .description(null) // <-- this is the key for branch coverage
                .timestamp(new LogTimestamp(Instant.parse("2026-03-18T10:00:00Z")))
                .build();

        AuditlogResponse response = invokeToResponse(log);

        assertEquals(id, response.id());
        assertEquals(actorRef, response.actorRef());
        assertNull(response.description()); // <-- verifies null branch
        }
        
    @Test
    void findById_shouldReturnMappedResponse_whenFound() {
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();

        Log log = log(
                id,
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "sys",
                "svc",
                "cmp",
                "{\"test\":true}",
                "description",
                Instant.parse("2026-03-18T10:00:00Z")
        );

        when(repository.findById(id)).thenReturn(Optional.of(log));

        Optional<AuditlogResponse> result = service.findById(id);

        assertTrue(result.isPresent());

        AuditlogResponse response = result.get();

        assertEquals(id, response.id());
        assertEquals(actorRef, response.actorRef());
        assertEquals(ActorType.USER, response.actorType());
        assertEquals(Severity.INFO, response.severity());
        assertEquals("sys", response.originSystem());
        assertEquals("svc", response.originService());
        assertEquals("cmp", response.originComponent());
        assertEquals("{\"test\":true}", response.data());
        assertEquals("description", response.description());
        assertEquals(Instant.parse("2026-03-18T10:00:00Z"), response.timestamp());

        verify(repository).findById(id);
}

    private AuditlogResponse invokeToResponse(Log log) {
        try {
                        var method = AuditlogApplicationService.class
                                .getDeclaredMethod("toResponse", Log.class);
                        method.setAccessible(true);
                        return (AuditlogResponse) method.invoke(service, log);
        } catch (Exception e) {
                        throw new RuntimeException(e);
        }
    }

    /*compensate */

    @Test
        void compensate_WhenEntityDoesNotExist_ShouldReturnIdempotentWithFalse() {
        // Given
        CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.COMPENSATE);

        UUID id = UUID.randomUUID();
        
        when(repository.findById(id)).thenReturn(null);

        // When
        ResponseCompensated result = service.compensate(id, this.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertFalse(result.success());
        verify(repository).findById(id);
        verify(repository, never()).compensate(any(), any());
   }

   @Test
   void compensate_WhenEntityExistsAndCompensateSucceeds_ShouldReturnCompensatedWithTrue() {
        // Given
        CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.COMPENSATE);
        
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        Log saved = log(
                savedId,
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"created\"}",
                "created from test",
                Instant.parse("2026-03-18T12:00:01Z")
        );

        
        when(repository.findById(id)).thenReturn(Optional.of(saved));
        when(repository.compensate(LogId.of(id), cmd.sagaState())).thenReturn(true);

        // When
        ResponseCompensated result = service.compensate(id, this.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.COMPENSATED, result.sagaState());
        assertTrue(result.success());
        verify(repository).findById(id);
        verify(repository).compensate(LogId.of(id), cmd.sagaState());
   }

   @Test
   void compensate_WhenEntityExistsAndCompensateFails_ShouldReturnIdempotentWithTrue() {
        // Given
        CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.COMPENSATE);
        UUID id = UUID.randomUUID();
        UUID actorRef = UUID.randomUUID();
                UUID savedId = UUID.randomUUID();
                Log saved = log(
                        savedId,
                        actorRef,
                        ActorType.USER,
                        Severity.INFO,
                        "audit-system",
                        "audit-service",
                        "audit-component",
                        "{\"event\":\"created\"}",
                        "created from test",
                        Instant.parse("2026-03-18T12:00:01Z")
                );
        
        when(repository.findById(id)).thenReturn(Optional.of(saved));
        when(repository.compensate(LogId.of(id), cmd.sagaState())).thenReturn(false);

        // When
        ResponseCompensated result = service.compensate(id, this.getClass(), cmd.sagaState());

        // Then
        assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
        assertTrue(result.success());
        verify(repository).findById(id);
        verify(repository).compensate(LogId.of(id), cmd.sagaState());
   }

  @Test
  void compensate_WhenRepositoryThrowsConflictException_ShouldPropagateException() {
    // Given
    CompensateAuditlogCmd cmd = new CompensateAuditlogCmd(String.class, SagaOutcome.SUCCEEDED);
    UUID id = UUID.randomUUID();

    UUID actorRef = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        Log saved = log(
                savedId,
                actorRef,
                ActorType.USER,
                Severity.INFO,
                "audit-system",
                "audit-service",
                "audit-component",
                "{\"event\":\"created\"}",
                "created from test",
                Instant.parse("2026-03-18T12:00:01Z")
        );
    
        when(repository.findById(id)).thenReturn(Optional.of(saved));
        when(repository.compensate(LogId.of(id), cmd.sagaState()))
                .thenThrow(new ConflictException("compensate.wrong_state", Map.of("compensate", cmd.sagaState().name())));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
                service.compensate(id, this.getClass(), cmd.sagaState());
        });
        
        assertEquals("compensate.wrong_state", exception.getMessage());
        verify(repository).findById(id);
        verify(repository).compensate(LogId.of(id), cmd.sagaState());
   }
}
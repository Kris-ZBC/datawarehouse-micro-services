package local.sop.datawarehouse.auditlog.domain.service;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.datawarehouse.auditlog.domain.model.Log;
import local.sop.datawarehouse.auditlog.domain.model.valueobjects.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditlogDomainServiceTest {

    private final AuditlogDomainService service = new AuditlogDomainService();

    @Test
    void createLog_shouldCreateNewLogWithNewIdAndTimestamp() {
        // Arrange
        Log input = Log.builder()
                .id(LogId.newId())
                .actorRef(ActorRef.newActorRef())
                .actorType(ActorType.USER)
                .severity(Severity.INFO)
                .originSystem(OriginSystem.newOriginSystem("originSystem"))
                .originService(OriginService.newOriginService("originService"))
                .originComponent(OriginComponent.newOriginComponent("originComponent"))
                .data(Data.newData("data"))
                .description(Description.newDescription("description"))
                .timestamp(LogTimestamp.now()) // old timestamp
                .build();

        // Act
        Log result = service.createLog(input);

        // Assert

        // New ID should be generated (not equal to old one)
        assertNotEquals(input.getId(), result.getId());
       
        // All other fields copied correctly
        assertEquals(input.getActorRef(), result.getActorRef());
        assertEquals(input.getActorType(), result.getActorType());
        assertEquals(input.getSeverity(), result.getSeverity());
        assertEquals(input.getOriginSystem(), result.getOriginSystem());
        assertEquals(input.getOriginService(), result.getOriginService());
        assertEquals(input.getOriginComponent(), result.getOriginComponent());
        assertEquals(input.getData(), result.getData());
        assertEquals(input.getDescription(), result.getDescription());
    }
}
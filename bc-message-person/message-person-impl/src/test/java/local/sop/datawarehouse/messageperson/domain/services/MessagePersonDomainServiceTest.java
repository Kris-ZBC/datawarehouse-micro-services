package local.sop.datawarehouse.messageperson.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.messageperson.domain.model.MessagePerson;
import local.sop.datawarehouse.messageperson.domain.model.valueobjects.CreatedAtTimestamp;
import local.sop.datawarehouse.messageperson.domain.service.MessagePersonDomainService;

class MessagePersonDomainServiceTest {

    private final MessagePersonDomainService service = new MessagePersonDomainService();

    private static final UUID MESSAGE_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(MESSAGE_REF, PERSON_REF);

    // ── createMessagePerson ────────────────────────────────────────────────────

    @Test
    void createMessagePerson_shouldReturnAggregate_withCorrectId() {
        MessagePerson result = service.createMessagePerson(VALID_KEY, true);
        assertEquals(VALID_KEY, result.getId());
    }

    @Test
    void createMessagePerson_shouldReturnAggregate_withActiveTrue() {
        MessagePerson result = service.createMessagePerson(VALID_KEY, true);
        assertTrue(result.isActive());
    }

    @Test
    void createMessagePerson_shouldReturnAggregate_withActiveFalse() {
        MessagePerson result = service.createMessagePerson(VALID_KEY, false);
        assertFalse(result.isActive());
    }

    @Test
    void createMessagePerson_shouldSetCreatedAt_toNow() {
        MessagePerson result = service.createMessagePerson(VALID_KEY, true);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedAt().value());
    }

    // ── toggleActivateMessagePerson ────────────────────────────────────────────

    @Test
    void toggleActivateMessagePerson_shouldReturnAggregate_withToggledActiveFromTrue() {
        MessagePerson result = service.toggleActivateMessagePerson(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertFalse(result.isActive());
    }

    @Test
    void toggleActivateMessagePerson_shouldReturnAggregate_withToggledActiveFromFalse() {
        MessagePerson result = service.toggleActivateMessagePerson(VALID_KEY, false, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertTrue(result.isActive());
    }

    @Test
    void toggleActivateMessagePerson_shouldPreserveId() {
        MessagePerson result = service.toggleActivateMessagePerson(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertEquals(VALID_KEY, result.getId());
    }

    // ── deleteMessagePerson ────────────────────────────────────────────────────

    @Test
    void deleteMessagePerson_shouldReturnAggregate_withActiveFalse() {
        MessagePerson result = service.deleteMessagePerson(VALID_KEY);
        assertFalse(result.isActive());
    }

    @Test
    void deleteMessagePerson_shouldPreserveId() {
        MessagePerson result = service.deleteMessagePerson(VALID_KEY);
        assertEquals(VALID_KEY, result.getId());
    }

}

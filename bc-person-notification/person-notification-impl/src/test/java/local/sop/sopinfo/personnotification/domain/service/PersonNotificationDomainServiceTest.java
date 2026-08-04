package local.sop.sopinfo.personnotification.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.personnotification.domain.model.PersonNotification;
import local.sop.sopinfo.personnotification.domain.model.valueobjects.CreatedAtTimestamp;

class PersonNotificationDomainServiceTest {

    private final PersonNotificationDomainService service = new PersonNotificationDomainService();

    private static final UUID NOTIFICATION_REF = UUID.randomUUID();
    private static final UUID PERSON_REF = UUID.randomUUID();
    private static final CompositeKey VALID_KEY = new CompositeKey(NOTIFICATION_REF, PERSON_REF);

    // ── createPersonNotification ────────────────────────────────────────────────────

    @Test
    void createPersonNotification_shouldReturnAggregate_withCorrectId() {
        PersonNotification result = service.createPersonNotification(VALID_KEY, true);
        assertEquals(VALID_KEY, result.getId());
    }

    @Test
    void createPersonNotification_shouldReturnAggregate_withActiveTrue() {
        PersonNotification result = service.createPersonNotification(VALID_KEY, true);
        assertTrue(result.isActive());
    }

    @Test
    void createPersonNotification_shouldReturnAggregate_withActiveFalse() {
        PersonNotification result = service.createPersonNotification(VALID_KEY, false);
        assertFalse(result.isActive());
    }

    @Test
    void createPersonNotification_shouldSetCreatedAt_toNow() {
        PersonNotification result = service.createPersonNotification(VALID_KEY, true);
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedAt().value());
    }

    // ── toggleActivatePersonNotification ────────────────────────────────────────────

    @Test
    void toggleActivatePersonNotification_shouldReturnAggregate_withToggledActiveFromTrue() {
        PersonNotification result = service.toggleActivatePersonNotification(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertFalse(result.isActive());
    }

    @Test
    void toggleActivatePersonNotification_shouldReturnAggregate_withToggledActiveFromFalse() {
        PersonNotification result = service.toggleActivatePersonNotification(VALID_KEY, false, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertTrue(result.isActive());
    }

    @Test
    void toggleActivatePersonNotification_shouldPreserveId() {
        PersonNotification result = service.toggleActivatePersonNotification(VALID_KEY, true, new CreatedAtTimestamp(LocalDateTime.now()).value());
        assertEquals(VALID_KEY, result.getId());
    }

    // ── deletePersonNotification ────────────────────────────────────────────────────

    @Test
    void deletePersonNotification_shouldReturnAggregate_withActiveFalse() {
        PersonNotification result = service.deletePersonNotification(VALID_KEY);
        assertFalse(result.isActive());
    }

    @Test
    void deletePersonNotification_shouldPreserveId() {
        PersonNotification result = service.deletePersonNotification(VALID_KEY);
        assertEquals(VALID_KEY, result.getId());
    }

}

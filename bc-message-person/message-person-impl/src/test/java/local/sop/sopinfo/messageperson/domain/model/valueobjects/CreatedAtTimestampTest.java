package local.sop.sopinfo.messageperson.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class CreatedAtTimestampTest {

    @Test
    void should_throw_exception_when_createdAt_is_null() {
        ValidationException exception = assertThrows(ValidationException.class, () -> new CreatedAtTimestamp(null));
        assertEquals("message-person.createdate.required", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_createdAt_is_in_future() {
        ValidationException exception = assertThrows(ValidationException.class, () -> new CreatedAtTimestamp(LocalDateTime.now().plusDays(1)));
        assertEquals("message-person.createdate.isafter", exception.getMessage());
    }

        @Test
    void shouldCreate_whenValueIsValid() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        CreatedAtTimestamp ts = new CreatedAtTimestamp(past);
        assertNotNull(ts);
        assertEquals(past, ts.value());
    }

    @Test
    void shouldCreate_whenValueIsNow() {
        // now() should be accepted — not in the future
        LocalDateTime now = LocalDateTime.now().minusNanos(1);
        CreatedAtTimestamp ts = new CreatedAtTimestamp(now);
        assertNotNull(ts);
    }

    @Test
    void shouldThrow_whenValueIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new CreatedAtTimestamp(null));
        assertEquals("message-person.createdate.required", ex.getMessage());
    }

    @Test
    void shouldThrow_whenValueIsInFuture() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> new CreatedAtTimestamp(LocalDateTime.now().plusDays(1)));
        assertEquals("message-person.createdate.isafter", ex.getMessage());
    }
    
}

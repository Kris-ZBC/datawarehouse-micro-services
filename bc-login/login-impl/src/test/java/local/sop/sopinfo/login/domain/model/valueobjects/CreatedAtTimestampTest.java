package local.sop.sopinfo.login.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class CreatedAtTimestampTest {

	@Test
	void shouldCreateCreatedAtTimestamp_whenValueIsNow() {
		LocalDateTime now = LocalDateTime.now();
		CreatedAtTimestamp ts = new CreatedAtTimestamp(now);
		assertEquals(now, ts.value());
	}

	@Test
	void shouldCreateCreatedAtTimestamp_whenValueIsPast() {
		LocalDateTime past = LocalDateTime.now().minusSeconds(5);
		CreatedAtTimestamp ts = new CreatedAtTimestamp(past);
		assertEquals(past, ts.value());
	}

	@Test
	void shouldThrowException_whenValueIsNull() {
		LocalDateTime now = null;
		assertThrows(ValidationException.class, () -> new CreatedAtTimestamp(now));
	}

	@Test
	void shouldThrowException_whenValueIsInFuture() {
		LocalDateTime future = LocalDateTime.now().plusSeconds(60);
		assertThrows(ValidationException.class, () -> new CreatedAtTimestamp(future));
	}
}

package local.sop.sopinfo.login.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public class ExpiresAtTimestampTest {
	@Test
	void shouldCreateExpiresAtTimestamp_whenValueIsInFuture() {
		LocalDateTime future = LocalDateTime.now().plusHours(2);
		ExpiresAtTimestamp ts = new ExpiresAtTimestamp(future);
		assertEquals(future, ts.value());
	}

	@Test
	void shouldThrowException_whenValueIsNull() {
		assertThrows(ValidationException.class, () -> new ExpiresAtTimestamp(null));
	}

	@Test
	void shouldThrowException_whenValueIsInPast() {
		LocalDateTime past = LocalDateTime.now().minusHours(2);
		assertThrows(ValidationException.class, () -> new ExpiresAtTimestamp(past));
	}
}
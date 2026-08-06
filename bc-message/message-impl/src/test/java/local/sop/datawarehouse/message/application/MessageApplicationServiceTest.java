package local.sop.datawarehouse.message.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.message.application.api.dto.CreateMessageCmd;
import local.sop.datawarehouse.message.application.api.dto.MessageResponse;
import local.sop.datawarehouse.message.application.mapper.MessageResponseMapper;
import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;
import local.sop.datawarehouse.message.domain.ports.out.MessageRepositoryPort;
import local.sop.datawarehouse.message.domain.service.MessageDomain;

class MessageApplicationServiceTest {

	private MessageDomain messageDomain;
	private MessageRepositoryPort messageRepositoryPort;
	private MessageResponseMapper messageResponseMapper;
	private MessageApplicationService service;

	@BeforeEach
	void setUp() {
		messageDomain = mock(MessageDomain.class);
		messageRepositoryPort = mock(MessageRepositoryPort.class);
		messageResponseMapper = mock(MessageResponseMapper.class);
		service = new MessageApplicationService(messageDomain, messageRepositoryPort, messageResponseMapper);
	}

	@Test
	@DisplayName("should create message")
	void shouldCreateMessage() {
		UUID sender = UUID.randomUUID();

		CreateMessageCmd cmd = new CreateMessageCmd(
				sender,
				"Hello");

		Message domainMessage = new Message(
				MessageId.newId(),
				OffsetDateTime.now(),
				"Hello",
				sender);

		Message savedMessage = new Message(
				domainMessage.id(),
				domainMessage.dateTimeSent(),
				domainMessage.message(),
				domainMessage.senderPersonRef()

		);

		MessageResponse expected = new MessageResponse(
				savedMessage.id().value(),
				savedMessage.dateTimeSent(),
				savedMessage.message(),
				savedMessage.senderPersonRef());

		when(messageDomain.create(sender, "Hello")).thenReturn(domainMessage);
		when(messageRepositoryPort.save(domainMessage)).thenReturn(savedMessage);
		when(messageResponseMapper.toResponse(savedMessage)).thenReturn(expected);

		MessageResponse actual = service.create(cmd);

		assertEquals(expected, actual);
		verify(messageDomain).create(sender, "Hello");
		verify(messageRepositoryPort).save(domainMessage);
		verify(messageResponseMapper).toResponse(savedMessage);
	}

	@Test
	@DisplayName("should throw when create cmd is null")
	void shouldThrowWhenCreateCmdIsNull() {
		assertThrows(ValidationException.class, () -> service.create(null));

		verifyNoInteractions(messageDomain, messageRepositoryPort, messageResponseMapper);
	}

	@Test
	@DisplayName("should find message by id")
	void shouldFindMessageById() {
		UUID id = UUID.randomUUID();
		UUID sender = UUID.randomUUID();

		Message message = new Message(
				MessageId.of(id),
				OffsetDateTime.now(),
				"Hello",
				sender);

		MessageResponse expected = new MessageResponse(
				id,
				message.dateTimeSent(),
				message.message(),
				sender);

		when(messageRepositoryPort.findById(MessageId.of(id))).thenReturn(java.util.Optional.of(message));
		when(messageResponseMapper.toResponse(message)).thenReturn(expected);

		MessageResponse actual = service.findById(id).get();

		assertEquals(expected, actual);
		verify(messageRepositoryPort).findById(MessageId.of(id));
		verify(messageResponseMapper).toResponse(message);
		verifyNoInteractions(messageDomain);
	}

	@Test
	@DisplayName("should throw when id is null in findById")
	void shouldThrowWhenIdIsNullInFindById() {
		assertThrows(ValidationException.class, () -> service.findById(null));

		verifyNoInteractions(messageDomain, messageRepositoryPort, messageResponseMapper);
	}

	@Test
	@DisplayName("should throw when message is not found")
	void shouldThrowWhenMessageIsNotFound() {
		UUID id = UUID.randomUUID();

		when(messageRepositoryPort.findById(MessageId.of(id))).thenReturn(java.util.Optional.empty());

		assertThrows(ValidationException.class, () -> service.findById(id));

		verify(messageRepositoryPort).findById(MessageId.of(id));
		verifyNoMoreInteractions(messageRepositoryPort);
		verifyNoInteractions(messageDomain, messageResponseMapper);
	}

	@Test
	void compensate_whenMessageNotFound_shouldReturnIdempotentFalse() {
    	UUID id = UUID.randomUUID();

    	when(messageRepositoryPort.findById(MessageId.of(id))).thenReturn(java.util.Optional.empty());

    	ResponseCompensated result = service.compensate(id, getClass(), SagaOutcome.COMPENSATE);

	    assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
    	assertFalse(result.success());
    	verify(messageRepositoryPort).findById(MessageId.of(id));
    	verify(messageRepositoryPort, never()).compensate(any(), any());
	}

	@Test
	void compensate_whenMessageExistsAndComensateSucceeds_shouldReturnCompensateTrue() {
		UUID id = UUID.randomUUID();
		UUID sender = UUID.randomUUID();

		Message message = new Message(MessageId.of(id), OffsetDateTime.now(), "Hello", sender);

		when(messageRepositoryPort.findById(MessageId.of(id))).thenReturn(java.util.Optional.of(message));
		when(messageRepositoryPort.compensate(MessageId.of(id), SagaOutcome.COMPENSATE)).thenReturn(true);

		ResponseCompensated result = service.compensate(id, getClass(), SagaOutcome.COMPENSATE);

		assertEquals(SagaOutcome.COMPENSATE, result.sagaState());
		assertTrue(result.success());
		verify(messageRepositoryPort).findById(MessageId.of(id));
		verify(messageRepositoryPort).compensate(MessageId.of(id), SagaOutcome.COMPENSATE);
	}

	@Test
	void compensate_whenMessageExistsButCompensateReturnsFalse_shouldReturnIdempodentFalse() {
		UUID id = UUID.randomUUID();
		UUID sender = UUID.randomUUID();

		Message message = new Message(MessageId.of(id), OffsetDateTime.now(), "Hello", sender);

		when(messageRepositoryPort.findById(MessageId.of(id))).thenReturn(java.util.Optional.of(message));
		when(messageRepositoryPort.compensate(MessageId.of(id), SagaOutcome.COMPENSATE)).thenReturn(false);

		ResponseCompensated result = service.compensate(id, getClass(), SagaOutcome.COMPENSATE);

		assertEquals(SagaOutcome.IDEMPOTENT, result.sagaState());
		assertFalse(result.success());
		verify(messageRepositoryPort).findById(MessageId.of(id));
		verify(messageRepositoryPort).compensate(MessageId.of(id), SagaOutcome.COMPENSATE);
	}
}
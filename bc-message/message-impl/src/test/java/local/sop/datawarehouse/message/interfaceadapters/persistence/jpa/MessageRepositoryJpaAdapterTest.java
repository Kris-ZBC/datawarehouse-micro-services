package local.sop.datawarehouse.message.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.message.domain.model.Message;
import local.sop.datawarehouse.message.domain.model.valueobjects.MessageId;

class MessageRepositoryJpaAdapterTest {

    private MessageSpringDataRepository repository;
    private MessageJpaMapper mapper;
    private MessageRepositoryJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(MessageSpringDataRepository.class);
        mapper = mock(MessageJpaMapper.class);
        adapter = new MessageRepositoryJpaAdapter(repository, mapper);
    }

    @Test
    @DisplayName("should save message")
    void shouldSaveMessage() {
        Message message = new Message(
            MessageId.newId(),
            OffsetDateTime.now(),
            "Hello",
            UUID.randomUUID()
        );

        MessageEntity entity = new MessageEntity();
        entity.withId(message.id().value());
        entity.withDateTimeSent(message.dateTimeSent());
        entity.withMessage(message.message());
        entity.withSenderPersonRef(message.senderPersonRef());

        when(mapper.toEntity(message)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(message);

        Message result = adapter.save(message);

        assertEquals(message, result);
        verify(mapper).toEntity(message);
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }

	@Test
	void compensate_whenWrongSagaState_shouldThrowConflictException() {
		MessageId id = MessageId.newId();

		assertThrows(ConflictException.class, () -> adapter.compensate(id, SagaOutcome.COMPENSATED));

		verifyNoInteractions(repository, mapper);
	}

	@Test
	void compensate_whenMessageNotFound_shouldReturnFalse() {
		MessageId id = MessageId.newId();

		when(repository.findById(id.value())).thenReturn(java.util.Optional.empty());

		Boolean result = adapter.compensate(id, SagaOutcome.COMPENSATE);

		assertFalse(result);
		verify(repository).findById(id.value());
		verify(repository, never()).delete(any(UUID.class));
	}

	@Test
	void compensate_whenMessageFoundAndDeleted_shouldReturnTrue() {
		    MessageId id = MessageId.newId();
    	MessageEntity entity = new MessageEntity();

    	when(repository.findById(id.value())).thenReturn(java.util.Optional.of(entity));
    	when(mapper.toDomain(entity)).thenReturn(mock(Message.class));
    	when(repository.delete(id.value())).thenReturn(1);

    	Boolean result = adapter.compensate(id, SagaOutcome.COMPENSATE);

    	assertTrue(result);
    	verify(repository).findById(id.value());
    	verify(repository).delete(id.value());
	}

	@Test
	void compensate_whenMessageFoundButDeleteFails_shouldReturnFalse() {
    	MessageId id = MessageId.newId();
    	MessageEntity entity = new MessageEntity();

    	when(repository.findById(id.value())).thenReturn(java.util.Optional.of(entity));
    	when(mapper.toDomain(entity)).thenReturn(mock(Message.class));
    	when(repository.delete(id.value())).thenReturn(0);

    	Boolean result = adapter.compensate(id, SagaOutcome.COMPENSATE);

    	assertFalse(result);
    	verify(repository).findById(id.value());
    	verify(repository).delete(id.value());
	}

}
package local.sop.datawarehouse.message.interfaceweb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.message.application.api.MessageDirectory;
import local.sop.datawarehouse.message.application.api.dto.CreateMessageCmd;
import local.sop.datawarehouse.message.application.api.dto.MessageResponse;

class InternalMessageControllerTest {

    private MessageDirectory messageDirectory;
    private InternalMessageController controller;

    @BeforeEach
    void setUp() {
        messageDirectory = mock(MessageDirectory.class);
        controller = new InternalMessageController(messageDirectory);
    }

    @Test
    @DisplayName("should delegate create to directory")
    void shouldDelegateCreateToDirectory() {
        CreateMessageCmd cmd = new CreateMessageCmd(
            UUID.randomUUID(),
            "Hello"
        );

        MessageResponse expected = new MessageResponse(
            UUID.randomUUID(),
            OffsetDateTime.now(),
            "Hello",
            cmd.senderPersonRef()
        );

        when(messageDirectory.create(cmd)).thenReturn(expected);

        MessageResponse result = controller.create(cmd);

        assertEquals(expected, result);
        verify(messageDirectory).create(cmd);
    }
}
package local.sop.sopinfo.notification.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.sopinfo.notification.application.api.NotificationDirectory;
import local.sop.sopinfo.notification.application.api.dto.CreateNotificationCmd;
import local.sop.sopinfo.notification.application.api.dto.NotificationResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=none",
	"bc.qualifier=notification"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InternalNotificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationDirectory directory;

	@Autowired
	private ObjectMapper objMapper;

	@Test
	void shouldReturn201_whenValidRequest() throws Exception {
		UUID id = UUID.randomUUID();
		CreateNotificationCmd cmd = new CreateNotificationCmd(UUID.randomUUID());
		when(directory.createNotification(any(CreateNotificationCmd.class))).thenReturn(id);

		mockMvc.perform(post("/internal/notifications/create")
			.with(user("registration-saga").roles("INTERNAL"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(objMapper.writeValueAsString(cmd)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(id.toString()));
		verify(directory, times(1)).createNotification(any(CreateNotificationCmd.class));
	}

	@Test
	void shouldReturn400_whenMessageRefIsNull() throws Exception {
		mockMvc.perform(post("/internal/notifications/create")
			.with(user("registration-saga").roles("INTERNAL"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"messageRef\": null}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/problem+json"))
			.andExpect(jsonPath("$.key").value("validation.error"));
	}

	@Test
	void shouldReturn400_whenMessageRefIsInvalid() throws Exception {
		mockMvc.perform(post("/internal/notifications/create")
			.with(user("registration-saga").roles("INTERNAL"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"messageRef\": \"eif\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType("application/problem+json"))
			.andExpect(jsonPath("$.key").value("request.body.invalid"));
	}

	@Test
	void shouldReturn500_whenServiceThrowsException() throws Exception {
		CreateNotificationCmd cmd = new CreateNotificationCmd(UUID.randomUUID());
		when(directory.createNotification(any(CreateNotificationCmd.class)))
			.thenThrow(new RuntimeException("Database error"));

		mockMvc.perform(post("/internal/notifications/create")
			.with(user("registration-saga").roles("INTERNAL"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(objMapper.writeValueAsString(cmd)))
			.andExpect(status().isInternalServerError());
		verify(directory, times(1)).createNotification(any(CreateNotificationCmd.class));
	}

	@Test
	void shouldReturn200_whenNotificationFound() throws Exception {
		UUID id = UUID.randomUUID();
		NotificationResponse response = new NotificationResponse(id, false, LocalDateTime.now(), UUID.randomUUID());
		when(directory.findById(id)).thenReturn(Optional.of(response));

		mockMvc.perform(get("/internal/notifications/{id}", id)
			.with(user("registration-saga").roles("INTERNAL")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id.toString()));
		verify(directory, times(1)).findById(id);
	}

	@Test
	void shouldReturn204_whenNotificationNotFound() throws Exception {
		UUID id = UUID.randomUUID();
		when(directory.findById(id)).thenReturn(Optional.empty());

		mockMvc.perform(get("/internal/notifications/{id}", id)
			.with(user("registration-saga").roles("INTERNAL")))
			.andExpect(status().isNoContent());
		verify(directory, times(1)).findById(id);
	}

	@Test
	void shouldReturn204_whenMarkingNotificationAsSeen() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(directory).makeNotificationSeen(id);

		mockMvc.perform(put("/internal/notifications/{id}/seen", id)
			.with(user("registration-saga").roles("INTERNAL")))
			.andExpect(status().isNoContent());
		verify(directory, times(1)).makeNotificationSeen(id);
	}

	@Test
	void shouldReturn204_whenDeletingNotification() throws Exception {
		UUID id = UUID.randomUUID();
		doNothing().when(directory).deleteNotification(id);

		mockMvc.perform(delete("/internal/notifications/{id}", id)
			.with(user("registration-saga").roles("INTERNAL")))
			.andExpect(status().isNoContent());
		verify(directory, times(1)).deleteNotification(id);
	}
}
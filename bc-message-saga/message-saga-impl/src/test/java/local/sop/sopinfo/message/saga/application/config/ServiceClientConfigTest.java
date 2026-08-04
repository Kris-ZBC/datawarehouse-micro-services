package local.sop.sopinfo.message.saga.application.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;

@ExtendWith(MockitoExtension.class)
public class ServiceClientConfigTest {

	@Mock
	private MtlsClientFactory clientFactory;

	@Mock
	private RestClient restClient;

	@Mock
	private ApprenticeProps apprenticeProps;

	@Mock
	private AuditLogProps auditlogProps;

	@Mock
	private EducationInstructorProps educationInstructorProps;

	@Mock
	private EducationLineProps educationLineProps;

	@Mock
	private InstructorProps instructorProps;

	@Mock
	private MessagePersonProps messagePersonProps;

	@Mock
	private MessageProps messageProps;

	@Mock
	private NotificationProps notificationProps;

	@Mock 
	private PersonNotificationProps personNotificationProps;

	@Mock
	private PersonProps personProps;

	private ServiceClientConfig config;

	@BeforeEach
	void setUp() {
		config = new ServiceClientConfig();
	}

	@Test
	void allBeansShouldCallFactoryWithCorrectArguments() {
		when(apprenticeProps.baseUrl()).thenReturn("A");
		when(auditlogProps.baseUrl()).thenReturn("B");
		when(educationInstructorProps.baseUrl()).thenReturn("C");
		when(educationLineProps.baseUrl()).thenReturn("D");
		when(instructorProps.baseUrl()).thenReturn("E");
		when(messagePersonProps.baseUrl()).thenReturn("F");
		when(messageProps.baseUrl()).thenReturn("G");
		when(notificationProps.baseUrl()).thenReturn("H");
		when(personNotificationProps.baseUrl()).thenReturn("I");
		when(personProps.baseUrl()).thenReturn("J");

		when(clientFactory.createMtlsClient(anyString(), anyString())).thenReturn(restClient);

		config.apprentice(clientFactory, apprenticeProps);
		config.auditLog(clientFactory, auditlogProps);
		config.educationInstructor(clientFactory, educationInstructorProps);
		config.educationline(clientFactory, educationLineProps);
		config.instructor(clientFactory, instructorProps);
		config.messagePerson(clientFactory, messagePersonProps);
		config.message(clientFactory, messageProps);
		config.notification(clientFactory, notificationProps);
		config.personNotification(clientFactory, personNotificationProps);
		config.person(clientFactory, personProps);

		verify(clientFactory).createMtlsClient("apprentice", "A");
		verify(clientFactory).createMtlsClient("auditlog", "B");
		verify(clientFactory).createMtlsClient("educationinstructor", "C");
		verify(clientFactory).createMtlsClient("educationline", "D");
		verify(clientFactory).createMtlsClient("instructor", "E");
		verify(clientFactory).createMtlsClient("messageperson", "F");
		verify(clientFactory).createMtlsClient("message", "G");
		verify(clientFactory).createMtlsClient("notification", "H");
		verify(clientFactory).createMtlsClient("personnotification", "I");
		verify(clientFactory).createMtlsClient("person", "J");
	}
}

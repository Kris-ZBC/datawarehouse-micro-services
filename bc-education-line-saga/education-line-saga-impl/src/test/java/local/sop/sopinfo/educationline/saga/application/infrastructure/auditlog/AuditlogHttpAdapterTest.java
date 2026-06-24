package local.sop.sopinfo.educationline.saga.application.infrastructure.auditlog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;

import local.sop.sopinfo.educationline.saga.application.api.dto.ResponseAuditlog;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadAuditlogCreate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.sopinfo.sharedkernel.enums.ActorType;
import local.sop.sopinfo.sharedkernel.enums.Severity;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.sagas.compensate.enums.SagaOutcome;

@ExtendWith(MockitoExtension.class)
public class AuditlogHttpAdapterTest {
	/*
	 * ----------------------------------------------------------- *
	 * AuditlogHttpAdapter tests
	 * -----------------------------------------------------------
	 */

	@Mock(name = "auditlog")
	private RestClient auditlogClient;

	private AuditlogHttpAdapter auditlogAdapter;

	@BeforeEach
	void setUp() {
		auditlogAdapter = new AuditlogHttpAdapter(auditlogClient);
	}

	@Test
	void auditlog_create_shouldReturnUuidFromDownstream() {
		UUID expectedId = UUID.randomUUID();
		PayloadAuditlogCreate request = new PayloadAuditlogCreate(
				UUID.randomUUID(), ActorType.USER, Severity.INFO,
				"originSystem", "originService", "originComponent",
				"data", "description");

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(auditlogClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri("/internal/auditlogs/create")).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadAuditlogCreate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseAuditlog.class)).thenReturn(new ResponseAuditlog(expectedId));

		UUID result = auditlogAdapter.create(
				request.actor(), request.type(), request.severity(),
				request.originSystem(), request.originService(), request.originComponent(),
				request.data(), request.description());

		assertEquals(expectedId, result);
		verify(auditlogClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/auditlogs/create");
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseAuditlog.class);
	}

	@Test
	void auditlog_create_shouldThrowExceptionWhenDownstreamReturnsNull() {
		PayloadAuditlogCreate request = new PayloadAuditlogCreate(
				UUID.randomUUID(), ActorType.USER, Severity.INFO,
				"originSystem", "originService", "originComponent",
				"data", "description");

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(auditlogClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri("/internal/auditlogs/create")).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadAuditlogCreate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseAuditlog.class)).thenReturn(null);

		assertThrows(ConflictException.class, () -> auditlogAdapter.create(
				request.actor(), request.type(), request.severity(),
				request.originSystem(), request.originService(), request.originComponent(),
				request.data(), request.description()));

		verify(auditlogClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/auditlogs/create");
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseAuditlog.class);
	}

	@Test
	void auditlog_compensate_shouldReturnResultFromDownstream() {
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, AuditlogHttpAdapter.class, state);
		ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(auditlogClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri("/internal/auditlogs/compensate")).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

		ResponseCompensated result = auditlogAdapter.compensate(id, AuditlogHttpAdapter.class, state);

		assertEquals(expectedResult.sagaState(), result.sagaState());
		assertEquals(expectedResult.success(), result.success());
		verify(auditlogClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/auditlogs/compensate");
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void auditlog_compensate_shouldThrowExceptionWhenDownstreamReturnsNull() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, AuditlogHttpAdapter.class, state);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RestClient.RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

		// When
		when(auditlogClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri("/internal/auditlogs/compensate")).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

		// Then
		assertThrows(ConflictException.class, () -> auditlogAdapter.compensate(id, AuditlogHttpAdapter.class, state));
		verify(auditlogClient).post();
		verify(mockRequestBodyUriSpec).uri("/internal/auditlogs/compensate");
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void auditlog_findById_shouldReturnUuidFromDownstream() {

		UUID expectedId = UUID.randomUUID();
		UUID inputId = UUID.randomUUID();

		@SuppressWarnings("rawtypes")
		RequestHeadersUriSpec mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

		ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(auditlogClient.get())
				.thenReturn(mockRequestHeadersUriSpec);

		when(mockRequestHeadersUriSpec.uri(
				"/internal/auditlogs/{id}",
				inputId))
				.thenReturn(mockRequestHeadersUriSpec);

		when(mockRequestHeadersUriSpec.retrieve())
				.thenReturn(mockResponseSpec);

		when(mockResponseSpec.body(ResponseAuditlog.class))
				.thenReturn(new ResponseAuditlog(expectedId));

		UUID result = auditlogAdapter.findById(inputId);

		assertEquals(expectedId, result);
	}

	@Test
	void auditlog_findById_shouldThrowConflictExceptionWhenDownstreamReturnsNull() {
		UUID inputId = UUID.randomUUID();

		@SuppressWarnings("rawtypes")
		RequestHeadersUriSpec mockRequestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

		ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

		when(auditlogClient.get())
				.thenReturn(mockRequestHeadersUriSpec);

		when(mockRequestHeadersUriSpec.uri(
				"/internal/auditlogs/{id}",
				inputId))
				.thenReturn(mockRequestHeadersUriSpec);

		when(mockRequestHeadersUriSpec.retrieve())
				.thenReturn(mockResponseSpec);

		when(mockResponseSpec.body(ResponseAuditlog.class))
				.thenReturn(null);

		assertThrows(ConflictException.class, () -> auditlogAdapter.findById(inputId));
	}

}

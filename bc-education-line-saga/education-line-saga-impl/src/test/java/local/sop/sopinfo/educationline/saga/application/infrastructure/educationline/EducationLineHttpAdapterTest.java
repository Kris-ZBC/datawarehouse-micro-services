package local.sop.sopinfo.educationline.saga.application.infrastructure.educationline;

//#region Imports
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
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

import local.sop.sopinfo.educationline.saga.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensateDuration;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadCompensateName;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadEducationLineCreate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadEducationLineDurationUpdate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.request.PayloadEducationLineNameUpdate;
import local.sop.sopinfo.educationline.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
//#endregion

@ExtendWith(MockitoExtension.class)
public class EducationLineHttpAdapterTest {
    
    /* ----------------------------------------------------------- *
     *  EducationLineHttpAdapter tests
     * ----------------------------------------------------------- */

    //#region Common setup
    @Mock(name = "educationLine")
    private RestClient educationLineClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private EducationLineHttpAdapter educationLineAdapter;
    private static final String BASE_URL = "/internal/educationlines";

    @BeforeEach
    void setUp() {
        educationLineAdapter = new EducationLineHttpAdapter(educationLineClient);
    }
    //#endregion

    //#region Post
    @Test
    void educationLine_create_shouldReturnEducationLineIdFromDownstream() {      
        // Given
        UUID expectedId = UUID.randomUUID();
        UUID educationRef = UUID.randomUUID();
        Instant createdAt = Instant.now();
        PayloadEducationLineCreate request = new PayloadEducationLineCreate("mockName", 365, 12, 7, educationRef);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationLineCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(
                new EducationLineResponse(expectedId, "mockName", 365, 12, 7, educationRef, createdAt, false));

        UUID actualId = educationLineAdapter.createEducationLine(request.name(), request.durationYears(),
                request.durationMonths(), request.durationDays(), request.educationRef());

        // Then
        assertEquals(expectedId, actualId);
        verify(educationLineClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_create_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID educationRef = UUID.randomUUID();
        PayloadEducationLineCreate request = new PayloadEducationLineCreate("mockName", 365, 12, 7, educationRef);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationLineCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
            educationLineAdapter.createEducationLine(request.name(), request.durationYears(),
                    request.durationMonths(), request.durationDays(), request.educationRef()));

        verify(educationLineClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }
    //#endregion

    //#region Get
    @Test
    void educationLine_get_shouldReturnEducationLineFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        EducationLineResponse expected = new EducationLineResponse(id, "mockName", 365, 12, 7, UUID.randomUUID(), Instant.now(), false);
        RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        doReturn(mockRequestHeadersUriSpec).when(educationLineClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(expected);

        EducationLineResponse actual = educationLineAdapter.findEducationLineById(id);

        // Then
        assertEquals(expected, actual);
        verify(educationLineClient).get();
        verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }
    //#endregion

    //#region Put
    @Test
    void educationLine_updateName_shouldReturnEducationLineIdFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        String newName = "newMockName";
        PayloadEducationLineNameUpdate request = new PayloadEducationLineNameUpdate(newName);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/name", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationLineNameUpdate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(
                new EducationLineResponse(id, newName, 365, 12, 7, UUID.randomUUID(), Instant.now(), false));

        UUID actualId = educationLineAdapter.updateEducationLineName(id, newName);

        // Then
        assertEquals(id, actualId);
        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/name", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_updateName_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String newName = "newMockName";
        PayloadEducationLineNameUpdate request = new PayloadEducationLineNameUpdate(newName);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/name", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationLineNameUpdate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
            educationLineAdapter.updateEducationLineName(id, newName));

        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/name", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }
    
    @Test
    void educationLine_updateDuration_shouldReturnEducationLineIdFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        int newDurationYears = 400;
        PayloadEducationLineDurationUpdate request = new PayloadEducationLineDurationUpdate(newDurationYears, 12, 7);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/duration", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationLineDurationUpdate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(
                new EducationLineResponse(id, "mockName", newDurationYears, 12, 7, UUID.randomUUID(), Instant.now(),
                        false));
                
        UUID actualId = educationLineAdapter.updateEducationLineDuration(id, newDurationYears, 12, 7);

        // Then
        assertEquals(id, actualId);
        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/duration", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_updateDuration_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        int newDurationYears = 400;
        PayloadEducationLineDurationUpdate request = new PayloadEducationLineDurationUpdate(newDurationYears, 12, 7);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/duration", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationLineDurationUpdate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
            educationLineAdapter.updateEducationLineDuration(id, newDurationYears, 12, 7));

        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/duration", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_deactivate_shouldReturnEducationLineIdFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(
                new EducationLineResponse(id, "mockName", 365, 12, 7, UUID.randomUUID(), Instant.now(), true));

        UUID actualId = educationLineAdapter.deactivateEducationLine(id);

        // Then
        assertEquals(id, actualId);
        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", id);
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_deactivate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
            educationLineAdapter.deactivateEducationLine(id));

        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", id);
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_activate_shouldReturnEducationLineIdFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(
                new EducationLineResponse(id, "mockName", 365, 12, 7, UUID.randomUUID(), Instant.now(), false));

        UUID actualId = educationLineAdapter.activateEducationLine(id);

        // Then
        assertEquals(id, actualId);
        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", id);
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }

    @Test
    void educationLine_activate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationLineResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
            educationLineAdapter.activateEducationLine(id));

        verify(educationLineClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", id);
        verify(mockResponseSpec).body(EducationLineResponse.class);
    }
    //#endregion

    //#region Compensate
    @Test
    void educationLine_compensate_shouldReturnResultFromDownstream() {

        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadCompensate request = new PayloadCompensate(id, EducationLineHttpAdapter.class, state);
        ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensate", id))
                .thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = educationLineAdapter.compensate(
                id, EducationLineHttpAdapter.class, state);

        // Then
        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(educationLineClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensate", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    
	@Test
	void educationLine_compensate_shouldThrowConflictExceptionWhenDownstreamReturnsNull() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, EducationLineHttpAdapter.class, state);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensate", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

		// Then
		assertThrows(ConflictException.class, () ->
			educationLineAdapter.compensate(id, EducationLineHttpAdapter.class, state));

		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensate", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void educationLine_compensateName_shouldReturnResultFromDownstream() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		String newName = "compensatedName";
		PayloadCompensateName request = new PayloadCompensateName(id, EducationLineHttpAdapter.class, state, newName);
		PayloadEducationLineNameUpdate nameReq = new PayloadEducationLineNameUpdate(newName);
		ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensatename", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensateName.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

		ResponseCompensated result = educationLineAdapter.compensateUpdateName(
				id, EducationLineHttpAdapter.class, state, nameReq.name());

		// Then
		assertEquals(expectedResult.sagaState(), result.sagaState());
		assertEquals(expectedResult.success(), result.success());
		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensatename", id);
		verify(mockRequestBodySpec).body(eq(request));
	}
	
	@Test
	void educationLine_compensateName_shouldThrowConflictExceptionWhenDownstreamReturnsNull() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		String newName = "compensatedName";
		PayloadCompensateName request = new PayloadCompensateName(id, EducationLineHttpAdapter.class, state, newName);
		PayloadEducationLineNameUpdate nameReq = new PayloadEducationLineNameUpdate(newName);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensatename", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensateName.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

		// Then
		assertThrows(ConflictException.class, () ->
			educationLineAdapter.compensateUpdateName(id, EducationLineHttpAdapter.class, state, nameReq.name()));

		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensatename", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void educationLine_compensateUpdateDuration_shouldReturnResultFromDownstream() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		int previousYears = 365;
		int previousMonths = 12;
		int previousDays = 7;
		PayloadCompensateDuration request = new PayloadCompensateDuration(id, EducationLineHttpAdapter.class, state, previousYears, previousMonths, previousDays);
		ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensateduration", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensateDuration.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

		ResponseCompensated result = educationLineAdapter.compensateUpdateDuration(
				id, EducationLineHttpAdapter.class, state, previousYears, previousMonths, previousDays);

		// Then
		assertEquals(expectedResult.sagaState(), result.sagaState());
		assertEquals(expectedResult.success(), result.success());
		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensateduration", id);
		verify(mockRequestBodySpec).body(eq(request));
	}

	@Test
	void educationLine_compensateUpdateDuration_shouldThrowConflictExceptionWhenDownstreamReturnsNull() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		int previousYears = 365;
		int previousMonths = 12;
		int previousDays = 7;
		PayloadCompensateDuration request = new PayloadCompensateDuration(id, EducationLineHttpAdapter.class, state, previousYears, previousMonths, previousDays);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensateduration", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensateDuration.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

		// Then
		assertThrows(ConflictException.class, () ->
			educationLineAdapter.compensateUpdateDuration(id, EducationLineHttpAdapter.class, state, previousYears, previousMonths, previousDays));

		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensateduration", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void educationLine_compensateActivate_shouldReturnResultFromDownstream() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, EducationLineHttpAdapter.class, state);
		ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensateactivate", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

		ResponseCompensated result = educationLineAdapter.compensateActivate(
				id, EducationLineHttpAdapter.class, state);
		
		// Then
		assertEquals(expectedResult.sagaState(), result.sagaState());
		assertEquals(expectedResult.success(), result.success());
		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensateactivate", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}
	
	@Test
	void educationLine_compensateActivate_shouldThrowConflictExceptionWhenDownstreamReturnsNull() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, EducationLineHttpAdapter.class, state);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensateactivate", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

		// Then
		assertThrows(ConflictException.class, () ->
			educationLineAdapter.compensateActivate(id, EducationLineHttpAdapter.class, state));

		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensateactivate", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void educationLine_compensateDeactivate_shouldReturnResultFromDownstream() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, EducationLineHttpAdapter.class, state);
		ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensatedeactivate", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

		ResponseCompensated result = educationLineAdapter.compensateDeactivate(
				id, EducationLineHttpAdapter.class, state);

		// Then
		assertEquals(expectedResult.sagaState(), result.sagaState());
		assertEquals(expectedResult.success(), result.success());
		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensatedeactivate", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}

	@Test
	void educationLine_compensateDeactivate_shouldThrowConflictExceptionWhenDownstreamReturnsNull() {
		// Given
		UUID id = UUID.randomUUID();
		SagaOutcome state = SagaOutcome.COMPENSATED;
		PayloadCompensate request = new PayloadCompensate(id, EducationLineHttpAdapter.class, state);

		RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
		RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
		ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

		// When
		when(educationLineClient.post()).thenReturn(mockRequestBodyUriSpec);
		when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/compensatedeactivate", id))
				.thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.body(any(PayloadCompensate.class))).thenReturn(mockRequestBodySpec);
		when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
		when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

		// Then
		assertThrows(ConflictException.class, () ->
			educationLineAdapter.compensateDeactivate(id, EducationLineHttpAdapter.class, state));

		verify(educationLineClient).post();
		verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/compensatedeactivate", id);
		verify(mockRequestBodySpec).body(eq(request));
		verify(mockResponseSpec).body(ResponseCompensated.class);
	}
	//#endregion
}

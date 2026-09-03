package local.sop.datawarehouse.education.saga.application.infrastructure.educationinstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateUpdate;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.datawarehouse.education.saga.application.api.dto.CreateEducationInstructorCmd;
import local.sop.datawarehouse.education.saga.application.api.dto.EducationInstructorResponse;
import local.sop.datawarehouse.education.saga.application.infrastructure.request.PayloadCreateEducationInstructor;

@ExtendWith(MockitoExtension.class)
public class EducationInstructorHttpAdapterTest {

    /* ----------------------------------------------------------- *
     *  EducationInstructorHttpAdapter tests
     * ----------------------------------------------------------- */

    //#region Common setup
    @Mock(name = "educationinstructor")
    private RestClient educationInstructorClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private EducationInstructorHttpAdapter educationInstructorAdapter;
    private UUID educationRef;
    private UUID instructorRef;
    private CreateEducationInstructorCmd createCmd;
    private CompositeKey compositeKey;
    private static final String BASE_URL = "/internal/education-instructors";

    @BeforeEach
    void setUp() {
        educationInstructorAdapter = new EducationInstructorHttpAdapter(educationInstructorClient);
        educationRef = UUID.randomUUID();
        instructorRef = UUID.randomUUID();
        compositeKey = new CompositeKey(educationRef, instructorRef);
        
        createCmd = new CreateEducationInstructorCmd(
            UUID.randomUUID(),
            compositeKey, 
            educationRef, 
            instructorRef, 
            null, 
            ActorType.USER, 
            Severity.INFO, 
            "null", 
            "null", 
            "null",
            "null", 
            "null"
        );
    }

    //#endregion

    //#region Post - createEducationInstructor
    @Test
    void educationInstructor_create_shouldReturnEducationInstructorResponseFromDownstream() {
        // Given
        CompositeKey expectedId = new CompositeKey(educationRef, instructorRef);
        PayloadCreateEducationInstructor request = new PayloadCreateEducationInstructor(compositeKey);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCreateEducationInstructor.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(
                new EducationInstructorResponse(compositeKey, Instant.now(), true));

        EducationInstructorResponse actualResponse = educationInstructorAdapter.createEducationInstructor(createCmd);
        CompositeKey actualKey = actualResponse.id();
 
        // Then
        assertEquals(expectedId.key1(), actualKey.key1());
        assertEquals(expectedId.key2(), actualKey.key2());
        verify(educationInstructorClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationInstructorResponse.class);
    }

    @Test 
    void educationInstructor_create_shouldThrowExceptionWhenDownstreamReturnNull() {
        // Given
        PayloadCreateEducationInstructor request = new PayloadCreateEducationInstructor(compositeKey);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCreateEducationInstructor.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationInstructorAdapter.createEducationInstructor(createCmd));
        verify(educationInstructorClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationInstructorResponse.class);
    }

    @Test
    void educationInstructor_create_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
        // Given
        PayloadCreateEducationInstructor request = new PayloadCreateEducationInstructor(compositeKey);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCreateEducationInstructor.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(
                new EducationInstructorResponse(null, Instant.now(), true));

        // Then
        assertThrows(ConflictException.class, () -> educationInstructorAdapter.createEducationInstructor(createCmd));
        verify(educationInstructorClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationInstructorResponse.class);
    }

    //#endregion

    //#region Get - findById
@Test
void educationInstructor_get_shouldReturnEducationInstructorResponseFromDownstream() {
    // Given
    EducationInstructorResponse expected = new EducationInstructorResponse(compositeKey, Instant.now(), true);
    RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    doReturn(mockRequestHeadersUriSpec).when(educationInstructorClient).get();
    doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", compositeKey);
    when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(expected);

    Optional<EducationInstructorResponse> actual = educationInstructorAdapter.findById(compositeKey);

    // Then
    assertEquals(expected, actual.get());
    verify(educationInstructorClient).get();
    verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}

@Test
void educationInstructor_get_shouldThrowExceptionWhenDownstreamReturnNull() {
    // Given
    RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    doReturn(mockRequestHeadersUriSpec).when(educationInstructorClient).get();
    doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", compositeKey);
    when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(null);

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.findById(compositeKey));
    verify(educationInstructorClient).get();
    verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}

@Test
void educationInstructor_get_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
    // Given
    RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    doReturn(mockRequestHeadersUriSpec).when(educationInstructorClient).get();
    doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", compositeKey);
    when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(
            new EducationInstructorResponse(null, Instant.now(), true));

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.findById(compositeKey));
    verify(educationInstructorClient).get();
    verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}
//#endregion

//#region Put - deactivateEducationInstructor
@Test
void educationInstructor_deactivate_shouldReturnEducationInstructorResponseFromDownstream() {
    // Given
    EducationInstructorResponse expected = new EducationInstructorResponse(compositeKey, Instant.now(), false);
    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.put()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", compositeKey)).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(expected);

    EducationInstructorResponse actual = educationInstructorAdapter.deactivateEducationInstructor(compositeKey);

    // Then
    assertEquals(expected, actual);
    verify(educationInstructorClient).put();
    verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}

@Test
void educationInstructor_deactivate_shouldThrowExceptionWhenDownstreamReturnNull() {
    // Given
    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.put()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", compositeKey)).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(null);

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.deactivateEducationInstructor(compositeKey));
    verify(educationInstructorClient).put();
    verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}

@Test
void educationInstructor_deactivate_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
    // Given
    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.put()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", compositeKey)).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(
            new EducationInstructorResponse(null, Instant.now(), false));

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.deactivateEducationInstructor(compositeKey));
    verify(educationInstructorClient).put();
    verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}
//#endregion

//#region Put - activateEducationInstructor
@Test
void educationInstructor_activate_shouldReturnEducationInstructorResponseFromDownstream() {
    // Given
    EducationInstructorResponse expected = new EducationInstructorResponse(compositeKey, Instant.now(), true);
    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.put()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", compositeKey)).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(expected);

    EducationInstructorResponse actual = educationInstructorAdapter.activateEducationInstructor(compositeKey);

    // Then
    assertEquals(expected, actual);
    verify(educationInstructorClient).put();
    verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}

@Test
void educationInstructor_activate_shouldThrowExceptionWhenDownstreamReturnNull() {
    // Given
    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.put()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", compositeKey)).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(null);

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.activateEducationInstructor(compositeKey));
    verify(educationInstructorClient).put();
    verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}

@Test
void educationInstructor_activate_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
    // Given
    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.put()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", compositeKey)).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(EducationInstructorResponse.class)).thenReturn(
            new EducationInstructorResponse(null, Instant.now(), true));

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.activateEducationInstructor(compositeKey));
    verify(educationInstructorClient).put();
    verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", compositeKey);
    verify(mockResponseSpec).body(EducationInstructorResponse.class);
}
//#endregion

//#region Post - compensateCreateEducationInstructor
@Test
void educationInstructor_compensateCreate_shouldReturnEducationInstructorResponseFromDownstream() {
    // Given
    SagaOutcome state = SagaOutcome.COMPENSATED;
    PayloadCompensateCreate request = new PayloadCompensateCreate(EducationInstructorHttpAdapter.class, state);
    ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-create",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

    ResponseCompensated result = educationInstructorAdapter.compensateCreateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state);

    // Then
    assertEquals(expectedResult.sagaState(), result.sagaState());
    assertEquals(expectedResult.success(), result.success());
    verify(educationInstructorClient).post();
    verify(mockRequestBodyUriSpec).uri("/education/{educationId}/instructor/{instructorId}/compensate-create",
            compositeKey.key1(), compositeKey.key2());
    verify(mockRequestBodySpec).body(eq(request));
    verify(mockResponseSpec).body(ResponseCompensated.class);
}

@Test
void educationInstructor_compensateCreate_shouldThrowExceptionWhenDownstreamReturnNull() {
    // Given
    SagaOutcome state = SagaOutcome.COMPENSATED;
    PayloadCompensateCreate request = new PayloadCompensateCreate(EducationInstructorHttpAdapter.class, state);

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-create",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.compensateCreateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state));
    verify(educationInstructorClient).post();
    verify(mockRequestBodyUriSpec).uri("/education/{educationId}/instructor/{instructorId}/compensate-create",
            compositeKey.key1(), compositeKey.key2());
    verify(mockRequestBodySpec).body(eq(request));
    verify(mockResponseSpec).body(ResponseCompensated.class);
}

@Test
void educationInstructor_compensateCreate_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
    // Given - this test is redundant as compensateCreate only checks response == null, not response.id()
    // keeping it to match the skeleton but it will never catch a regression the null test doesn't already catch
    SagaOutcome state = SagaOutcome.COMPENSATED;

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-create",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateCreate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(new ResponseCompensated(null, false));

    // Then - no exception thrown because adapter does not check response.sagaState() == null
    ResponseCompensated result = educationInstructorAdapter.compensateCreateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state);
    assertEquals(false, result.success());
}
//#endregion

//#region Post - compensateActivateEducationInstructor
@Test
void educationInstructor_compensateActivate_shouldReturnEducationInstructorResponseFromDownstream() {
    // Given
    SagaOutcome state = SagaOutcome.COMPENSATED;
    PayloadCompensateUpdate request = new PayloadCompensateUpdate(EducationInstructorHttpAdapter.class, state, null, null);
    ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-activate",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateUpdate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

    ResponseCompensated result = educationInstructorAdapter.compensateActivateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state);

    // Then
    assertEquals(expectedResult.sagaState(), result.sagaState());
    assertEquals(expectedResult.success(), result.success());
    verify(educationInstructorClient).post();
    verify(mockRequestBodyUriSpec).uri("/education/{educationId}/instructor/{instructorId}/compensate-activate",
            compositeKey.key1(), compositeKey.key2());
    verify(mockRequestBodySpec).body(eq(request));
    verify(mockResponseSpec).body(ResponseCompensated.class);
}

@Test
void educationInstructor_compensateActivate_shouldThrowExceptionWhenDownstreamReturnNull() {
    // Given
    SagaOutcome state = SagaOutcome.COMPENSATED;
    PayloadCompensateUpdate request = new PayloadCompensateUpdate(EducationInstructorHttpAdapter.class, state, null, null);

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-activate",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateUpdate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.compensateActivateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state));
    verify(educationInstructorClient).post();
    verify(mockRequestBodyUriSpec).uri("/education/{educationId}/instructor/{instructorId}/compensate-activate",
            compositeKey.key1(), compositeKey.key2());
    verify(mockRequestBodySpec).body(eq(request));
    verify(mockResponseSpec).body(ResponseCompensated.class);
}

@Test
void educationInstructor_compensateActivate_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
    // Given - same caveat as compensateCreate: adapter only checks response == null
    SagaOutcome state = SagaOutcome.COMPENSATED;

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-activate",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateUpdate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(new ResponseCompensated(null, false));

    ResponseCompensated result = educationInstructorAdapter.compensateActivateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state);
    assertEquals(false, result.success());
}
//#endregion

//#region Post - compensateDeactivateEducationInstructor
@Test
void educationInstructor_compensateDeactivate_shouldReturnEducationInstructorResponseFromDownstream() {
    // Given
    SagaOutcome state = SagaOutcome.COMPENSATED;
    PayloadCompensateUpdate request = new PayloadCompensateUpdate(EducationInstructorHttpAdapter.class, state, null, null);
    ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-deactivate",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateUpdate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

    ResponseCompensated result = educationInstructorAdapter.compensateDeactivateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state);

    // Then
    assertEquals(expectedResult.sagaState(), result.sagaState());
    assertEquals(expectedResult.success(), result.success());
    verify(educationInstructorClient).post();
    verify(mockRequestBodyUriSpec).uri("/education/{educationId}/instructor/{instructorId}/compensate-deactivate",
            compositeKey.key1(), compositeKey.key2());
    verify(mockRequestBodySpec).body(eq(request));
    verify(mockResponseSpec).body(ResponseCompensated.class);
}

@Test
void educationInstructor_compensateDeactivate_shouldThrowExceptionWhenDownstreamReturnNull() {
    // Given
    SagaOutcome state = SagaOutcome.COMPENSATED;
    PayloadCompensateUpdate request = new PayloadCompensateUpdate(EducationInstructorHttpAdapter.class, state, null, null);

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    // When
    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-deactivate",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateUpdate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

    // Then
    assertThrows(ConflictException.class, () -> educationInstructorAdapter.compensateDeactivateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state));
    verify(educationInstructorClient).post();
    verify(mockRequestBodyUriSpec).uri("/education/{educationId}/instructor/{instructorId}/compensate-deactivate",
            compositeKey.key1(), compositeKey.key2());
    verify(mockRequestBodySpec).body(eq(request));
    verify(mockResponseSpec).body(ResponseCompensated.class);
}

@Test
void educationInstructor_compensateDeactivate_shouldThrowExceptionWhenDownstreamReturnWithNullId() {
    // Given - same caveat: adapter only checks response == null
    SagaOutcome state = SagaOutcome.COMPENSATED;

    RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
    RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
    ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

    when(educationInstructorClient.post()).thenReturn(mockRequestBodyUriSpec);
    when(mockRequestBodyUriSpec.uri("/education/{educationId}/instructor/{instructorId}/compensate-deactivate",
            compositeKey.key1(), compositeKey.key2())).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.body(any(PayloadCompensateUpdate.class))).thenReturn(mockRequestBodySpec);
    when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
    when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(new ResponseCompensated(null, false));

    ResponseCompensated result = educationInstructorAdapter.compensateDeactivateEducationInstructor(
            compositeKey, EducationInstructorHttpAdapter.class, state);
    assertEquals(false, result.success());
}
//#endregion

}

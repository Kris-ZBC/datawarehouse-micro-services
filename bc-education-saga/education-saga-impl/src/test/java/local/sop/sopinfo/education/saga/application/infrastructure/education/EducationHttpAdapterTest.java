package local.sop.sopinfo.education.saga.application.infrastructure.education;

//#region Imports
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import local.sop.sopinfo.education.saga.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.saga.application.api.dto.EducationResponse;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadCreateCompensate;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadEducationCreate;
import local.sop.sopinfo.education.saga.application.infrastructure.request.PayloadUpdateCompensate;
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
//#endregion

@ExtendWith(MockitoExtension.class)
public class EducationHttpAdapterTest {

    /* ----------------------------------------------------------- *
     *  EducationHttpAdapter tests
     * ----------------------------------------------------------- */

    //#region Common setup
    @Mock(name = "education")
    private RestClient educationClient;

    @Mock
    RestClient.RequestHeadersUriSpec<?> mockRequestHeadersUriSpec;

    private EducationHttpAdapter educationAdapter;
    private CreateEducationCmd createCmd;
    private static final String BASE_URL = "/internal/educations";

    @BeforeEach
    void setUp() {
        educationAdapter = new EducationHttpAdapter(educationClient);

        createCmd = new CreateEducationCmd(
            UUID.randomUUID(),
            "mockName",
            "mockCategory",
            UUID.randomUUID(),
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

    //#region Post - createEducation
    @Test
    void education_create_shouldReturnEducationIdFromDownstream() {
        // Given
        UUID expectedId = UUID.randomUUID();
        PayloadEducationCreate request = new PayloadEducationCreate(createCmd.name(), createCmd.category());

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(expectedId, "mockName", "mockCategory", false));

        UUID actualId = educationAdapter.createEducation(createCmd);

        // Then
        assertEquals(expectedId, actualId);
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_create_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        PayloadEducationCreate request = new PayloadEducationCreate(createCmd.name(), createCmd.category());

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.createEducation(createCmd));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_create_shouldThrowExceptionWhenDownstreamReturnsResponseWithNullId() {
        // Given
        PayloadEducationCreate request = new PayloadEducationCreate(createCmd.name(), createCmd.category());

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/create")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadEducationCreate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(null, "mockName", "mockCategory", false));

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.createEducation(createCmd));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/create");
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(EducationResponse.class);
    }
    //#endregion

    //#region Get - findEducationById
    @Test
    void education_get_shouldReturnEducationFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        EducationResponse expected = new EducationResponse(id, "mockName", "mockCategory", false);
        RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        doReturn(mockRequestHeadersUriSpec).when(educationClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(expected);

        EducationResponse actual = educationAdapter.findEducationById(id);

        // Then
        assertEquals(expected, actual);
        verify(educationClient).get();
        verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_get_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        doReturn(mockRequestHeadersUriSpec).when(educationClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.findEducationById(id));
        verify(educationClient).get();
        verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_get_shouldThrowExceptionWhenDownstreamReturnsResponseWithNullId() {
        // Given
        UUID id = UUID.randomUUID();
        RequestHeadersUriSpec<?> mockRequestHeadersSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        doReturn(mockRequestHeadersUriSpec).when(educationClient).get();
        doReturn(mockRequestHeadersSpec).when(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(null, "mockName", "mockCategory", false));

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.findEducationById(id));
        verify(educationClient).get();
        verify(mockRequestHeadersUriSpec).uri(BASE_URL + "/{id}", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }
    //#endregion

    //#region Put - updateEducationName
    @Test
    void education_updateName_shouldReturnUpdatedEducationFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        String newName = "newMockName";

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/name", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(newName)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(id, newName, "mockCategory", false));

        EducationResponse actual = educationAdapter.updateEducationName(id, newName);

        // Then
        assertNotNull(actual);
        assertEquals(id, actual.id());
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/name", id);
        verify(mockRequestBodySpec).body(newName);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_updateName_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String newName = "newMockName";

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/name", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(newName)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.updateEducationName(id, newName));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/name", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_updateName_shouldThrowExceptionWhenDownstreamReturnsResponseWithNullId() {
        // Given
        UUID id = UUID.randomUUID();
        String newName = "newMockName";

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/name", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(newName)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(null, newName, "mockCategory", false));

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.updateEducationName(id, newName));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/name", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }
    //#endregion

    //#region Put - updateEducationCategory
    @Test
    void education_updateCategory_shouldReturnUpdatedEducationFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        String newCategory = "newMockCategory";

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/category", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(newCategory)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(id, "mockName", newCategory, false));

        EducationResponse actual = educationAdapter.updateEducationCategory(id, newCategory);

        // Then
        assertNotNull(actual);
        assertEquals(id, actual.id());
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/category", id);
        verify(mockRequestBodySpec).body(newCategory);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_updateCategory_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        String newCategory = "newMockCategory";

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/category", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(newCategory)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.updateEducationCategory(id, newCategory));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/category", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_updateCategory_shouldThrowExceptionWhenDownstreamReturnsResponseWithNullId() {
        // Given
        UUID id = UUID.randomUUID();
        String newCategory = "newMockCategory";

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/category", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(newCategory)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(null, "mockName", newCategory, false));

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.updateEducationCategory(id, newCategory));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/category", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }
    //#endregion

    //#region Put - activateEducation
    @Test
    void education_activate_shouldReturnEducationFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(id, "mockName", "mockCategory", false));

        EducationResponse actual = educationAdapter.activateEducation(id);

        // Then
        assertNotNull(actual);
        assertEquals(id, actual.id());
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_activate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.activateEducation(id));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_activate_shouldThrowExceptionWhenDownstreamReturnsResponseWithNullId() {
        // Given
        UUID id = UUID.randomUUID();

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/activate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(null, "mockName", "mockCategory", false));

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.activateEducation(id));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/activate", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }
    //#endregion

    //#region Put - deactivateEducation
    @Test
    void education_deactivate_shouldReturnEducationFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(id, "mockName", "mockCategory", false));

        EducationResponse actual = educationAdapter.deactivateEducation(id);

        // Then
        assertNotNull(actual);
        assertEquals(id, actual.id());
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_deactivate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.deactivateEducation(id));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }

    @Test
    void education_deactivate_shouldThrowExceptionWhenDownstreamReturnsResponseWithNullId() {
        // Given
        UUID id = UUID.randomUUID();

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.put()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/{id}/deactivate", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(EducationResponse.class)).thenReturn(
                new EducationResponse(null, "mockName", "mockCategory", false));

        // Then
        assertThrows(ConflictException.class, () -> educationAdapter.deactivateEducation(id));
        verify(educationClient).put();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/{id}/deactivate", id);
        verify(mockResponseSpec).body(EducationResponse.class);
    }
    //#endregion

    //#region Compensate - compensateCreateEducation
    @Test
    void education_compensateCreate_shouldReturnResultFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadCreateCompensate request = new PayloadCreateCompensate(id, EducationHttpAdapter.class, state);
        ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensatecreate/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCreateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = educationAdapter.compensateCreateEducation(id, EducationHttpAdapter.class, state);

        // Then
        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensatecreate/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void education_compensateCreate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadCreateCompensate request = new PayloadCreateCompensate(id, EducationHttpAdapter.class, state);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensatecreate/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadCreateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
                educationAdapter.compensateCreateEducation(id, EducationHttpAdapter.class, state));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensatecreate/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    //#endregion

    //#region Compensate - compensateUpdateEducationName
    @Test
    void education_compensateUpdateName_shouldReturnResultFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        String previousName = "previousName";
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, previousName, null);
        ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensateupdatename/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = educationAdapter.compensateUpdateEducationName(id, EducationHttpAdapter.class, state, previousName);

        // Then
        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensateupdatename/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void education_compensateUpdateName_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        String previousName = "previousName";
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, previousName, null);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensateupdatename/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
                educationAdapter.compensateUpdateEducationName(id, EducationHttpAdapter.class, state, previousName));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensateupdatename/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    //#endregion

    //#region Compensate - compensateUpdateEducationCategory
    @Test
    void education_compensateUpdateCategory_shouldReturnResultFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        String previousCategory = "previousCategory";
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, previousCategory, null);
        ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensateupdatecategory/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = educationAdapter.compensateUpdateEducationCategory(id, EducationHttpAdapter.class, state, previousCategory);

        // Then
        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensateupdatecategory/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void education_compensateUpdateCategory_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        String previousCategory = "previousCategory";
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, previousCategory, null);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensateupdatecategory/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
                educationAdapter.compensateUpdateEducationCategory(id, EducationHttpAdapter.class, state, previousCategory));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensateupdatecategory/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    //#endregion

    //#region Compensate - compensateActivateEducation
    @Test
    void education_compensateActivate_shouldReturnResultFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, null, null);
        ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensateactivate/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = educationAdapter.compensateActivateEducation(id, EducationHttpAdapter.class, state);

        // Then
        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensateactivate/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void education_compensateActivate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, null, null);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensateactivate/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
                educationAdapter.compensateActivateEducation(id, EducationHttpAdapter.class, state));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensateactivate/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    //#endregion

    //#region Compensate - compensateDeactivateEducation
    @Test
    void education_compensateDeactivate_shouldReturnResultFromDownstream() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, null, null);
        ResponseCompensated expectedResult = new ResponseCompensated(SagaOutcome.COMPENSATED, true);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensatedeactivate/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(expectedResult);

        ResponseCompensated result = educationAdapter.compensateDeactivateEducation(id, EducationHttpAdapter.class, state);

        // Then
        assertEquals(expectedResult.sagaState(), result.sagaState());
        assertEquals(expectedResult.success(), result.success());
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensatedeactivate/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }

    @Test
    void education_compensateDeactivate_shouldThrowExceptionWhenDownstreamReturnsNull() {
        // Given
        UUID id = UUID.randomUUID();
        SagaOutcome state = SagaOutcome.COMPENSATED;
        PayloadUpdateCompensate request = new PayloadUpdateCompensate(id, EducationHttpAdapter.class, state, null, null);

        RequestBodyUriSpec mockRequestBodyUriSpec = mock(RequestBodyUriSpec.class);
        RequestBodySpec mockRequestBodySpec = mock(RequestBodySpec.class);
        ResponseSpec mockResponseSpec = mock(ResponseSpec.class);

        // When
        when(educationClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(BASE_URL + "/compensatedeactivate/{id}", id)).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.body(any(PayloadUpdateCompensate.class))).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.body(ResponseCompensated.class)).thenReturn(null);

        // Then
        assertThrows(ConflictException.class, () ->
                educationAdapter.compensateDeactivateEducation(id, EducationHttpAdapter.class, state));
        verify(educationClient).post();
        verify(mockRequestBodyUriSpec).uri(BASE_URL + "/compensatedeactivate/{id}", id);
        verify(mockRequestBodySpec).body(eq(request));
        verify(mockResponseSpec).body(ResponseCompensated.class);
    }
    //#endregion
}
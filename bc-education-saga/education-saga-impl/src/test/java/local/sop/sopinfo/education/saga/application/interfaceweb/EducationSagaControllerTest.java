package local.sop.sopinfo.education.saga.application.interfaceweb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.springframework.http.ResponseEntity;

import local.sop.sopinfo.education.saga.application.api.EducationSagaDirectory;
import local.sop.sopinfo.education.saga.application.api.dto.CreateAuditlog;
import local.sop.sopinfo.education.saga.application.api.dto.CreateEducationCmd;
import local.sop.sopinfo.education.saga.application.api.dto.CreateEducationInstructorCmd;
import local.sop.sopinfo.education.saga.application.api.dto.EducationInstructorResponse;
import local.sop.sopinfo.education.saga.application.api.dto.EducationResponse;
import local.sop.sopinfo.education.saga.application.api.dto.UpdateEducationCategoryCmd;
import local.sop.sopinfo.education.saga.application.api.dto.UpdateEducationNameCmd;
import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;

@ExtendWith(MockitoExtension.class)
class EducationSagaControllerTest {

    @Mock
    private EducationSagaDirectory directory;

    private EducationSagaController controller;

    private static final String BASE_URL = "/internal/saga/educations";

    @BeforeEach
    void setUp() {
        controller = new EducationSagaController(directory);
    }

    // ------------------------------------------------------------------
    // createEducation
    // ------------------------------------------------------------------

    @Test
    void createEducation_shouldReturnCreatedResponse() {
        UUID id = UUID.randomUUID();

        CreateEducationCmd cmd = mock(CreateEducationCmd.class);
        EducationResponse response = mock(EducationResponse.class);

        when(response.id()).thenReturn(id);
        when(directory.createEducation(cmd)).thenReturn(response);

        ResponseEntity<EducationResponse> actual =
                controller.createEducation(cmd);

        assertEquals(201, actual.getStatusCode().value());
        assertSame(response, actual.getBody());
        assertEquals(
                BASE_URL + "/" + id,
                actual.getHeaders().getLocation().toString());

        verify(directory).createEducation(cmd);
    }

    @Test
    void createEducation_shouldPropagateException() {
        CreateEducationCmd cmd = mock(CreateEducationCmd.class);

        when(directory.createEducation(cmd))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class,
                () -> controller.createEducation(cmd));

        verify(directory).createEducation(cmd);
    }

    // ------------------------------------------------------------------
    // updateEducationName
    // ------------------------------------------------------------------

    @Test
    void updateEducationName_shouldReturnOkResponse() {
        UUID id = UUID.randomUUID();

        UpdateEducationNameCmd cmd = mock(UpdateEducationNameCmd.class);
        EducationResponse response = mock(EducationResponse.class);

        when(response.id()).thenReturn(id);
        when(directory.updateEducationName(id, cmd)).thenReturn(response);

        ResponseEntity<EducationResponse> actual =
                controller.updateEducationName(cmd, id);

        assertEquals(200, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).updateEducationName(id, cmd);
    }

    @Test
    void updateEducationName_shouldPropagateException() {
        UUID id = UUID.randomUUID();

        UpdateEducationNameCmd cmd = mock(UpdateEducationNameCmd.class);

        when(directory.updateEducationName(id, cmd))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.updateEducationName(cmd, id));
    }

    // ------------------------------------------------------------------
    // updateEducationCategory
    // ------------------------------------------------------------------

    @Test
    void updateEducationCategory_shouldReturnOkResponse() {
        UUID id = UUID.randomUUID();

        UpdateEducationCategoryCmd cmd = mock(UpdateEducationCategoryCmd.class);
        EducationResponse response = mock(EducationResponse.class);

        when(response.id()).thenReturn(id);
        when(directory.updateEducationCategory(id, cmd)).thenReturn(response);

        ResponseEntity<EducationResponse> actual =
                controller.updateEducationCategory(cmd, id);

        assertEquals(200, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).updateEducationCategory(id, cmd);
    }

    @Test
    void updateEducationCategory_shouldPropagateException() {
        UUID id = UUID.randomUUID();

        UpdateEducationCategoryCmd cmd = mock(UpdateEducationCategoryCmd.class);

        when(directory.updateEducationCategory(id, cmd))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.updateEducationCategory(cmd, id));
    }

    // ------------------------------------------------------------------
    // activateEducation
    // ------------------------------------------------------------------

    @Test
    void activateEducation_shouldReturnOkResponse() {
        UUID id = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);
        EducationResponse response = mock(EducationResponse.class);

        when(response.id()).thenReturn(id);
        when(directory.activateEducation(id, audit)).thenReturn(response);

        ResponseEntity<EducationResponse> actual =
                controller.activateEducation(audit, id);

        assertEquals(200, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).activateEducation(id, audit);
    }

    @Test
    void activateEducation_shouldPropagateException() {
        UUID id = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);

        when(directory.activateEducation(id, audit))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.activateEducation(audit, id));
    }

    // ------------------------------------------------------------------
    // deactivateEducation
    // ------------------------------------------------------------------

    @Test
    void deactivateEducation_shouldReturnOkResponse() {
        UUID id = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);
        EducationResponse response = mock(EducationResponse.class);

        when(response.id()).thenReturn(id);
        when(directory.deactivateEducation(id, audit)).thenReturn(response);

        ResponseEntity<EducationResponse> actual =
                controller.deactivateEducation(audit, id);

        assertEquals(200, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).deactivateEducation(id, audit);
    }

    @Test
    void deactivateEducation_shouldPropagateException() {
        UUID id = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);

        when(directory.deactivateEducation(id, audit))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.deactivateEducation(audit, id));
    }

    // ------------------------------------------------------------------
    // createEducationInstructor
    // ------------------------------------------------------------------

    @Test
    void createEducationInstructor_shouldReturnCreatedResponse() {
        UUID educationId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        CreateEducationInstructorCmd cmd =
                mock(CreateEducationInstructorCmd.class);

        CompositeKey key = new CompositeKey(
                educationId,
                instructorId);

        EducationInstructorResponse response =
                mock(EducationInstructorResponse.class);

        when(response.id()).thenReturn(key);
        when(directory.createEducationInstructor(cmd))
                .thenReturn(response);

        ResponseEntity<EducationInstructorResponse> actual =
                controller.createEducationInstructor(cmd);

        assertEquals(201, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).createEducationInstructor(cmd);
    }

    @Test
    void createEducationInstructor_shouldPropagateException() {
        CreateEducationInstructorCmd cmd =
                mock(CreateEducationInstructorCmd.class);

        when(directory.createEducationInstructor(cmd))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.createEducationInstructor(cmd));
    }

    // ------------------------------------------------------------------
    // activateEducationInstructor
    // ------------------------------------------------------------------

    @Test
    void activateEducationInstructor_shouldReturnOkResponse() {
        UUID educationId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);

        CompositeKey key = new CompositeKey(
                educationId,
                instructorId);

        EducationInstructorResponse response =
                mock(EducationInstructorResponse.class);

        when(response.id()).thenReturn(key);

        when(directory.activateEducationInstructor(
        any(CompositeKey.class),
        eq(audit)))
        .thenReturn(response);

        ResponseEntity<EducationInstructorResponse> actual =
                controller.activateEducationInstructor(
                        audit,
                        educationId,
                        instructorId);

        assertEquals(200, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).activateEducationInstructor(
        any(CompositeKey.class),
        eq(audit));
    }

    @Test
    void activateEducationInstructor_shouldPropagateException() {
        UUID educationId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);

        CompositeKey key = new CompositeKey(
                educationId,
                instructorId);

        when(directory.activateEducationInstructor(key, audit))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.activateEducationInstructor(
                        audit,
                        educationId,
                        instructorId));
    }

    // ------------------------------------------------------------------
    // deactivateEducationInstructor
    // ------------------------------------------------------------------

    @Test
    void deactivateEducationInstructor_shouldReturnOkResponse() {
        UUID educationId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);

        CompositeKey key = new CompositeKey(
                educationId,
                instructorId);

        EducationInstructorResponse response =
                mock(EducationInstructorResponse.class);

        when(response.id()).thenReturn(key);

        when(directory.deactivateEducationInstructor(
        any(CompositeKey.class),
        eq(audit)))
        .thenReturn(response);

        ResponseEntity<EducationInstructorResponse> actual =
                controller.deactivateEducationInstructor(
                        audit,
                        educationId,
                        instructorId);

        assertEquals(200, actual.getStatusCode().value());
        assertSame(response, actual.getBody());

        verify(directory).deactivateEducationInstructor(
        any(CompositeKey.class),
        eq(audit));
    }

    @Test
    void deactivateEducationInstructor_shouldPropagateException() {
        UUID educationId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();

        CreateAuditlog audit = mock(CreateAuditlog.class);

        CompositeKey key = new CompositeKey(
                educationId,
                instructorId);

        when(directory.deactivateEducationInstructor(key, audit))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> controller.deactivateEducationInstructor(
                        audit,
                        educationId,
                        instructorId));
    }
}
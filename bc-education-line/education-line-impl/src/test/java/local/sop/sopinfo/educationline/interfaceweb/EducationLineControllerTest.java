package local.sop.sopinfo.educationline.interfaceweb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import local.sop.sopinfo.educationline.application.api.EducationLineDirectory;
import local.sop.sopinfo.educationline.application.api.dto.CreateEducationLineCmd;
import local.sop.sopinfo.educationline.application.api.dto.EducationLineResponse;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineDurationCmd;
import local.sop.sopinfo.educationline.application.api.dto.UpdateEducationLineNameCmd;
import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.sagas.compensate.request.PayloadCompensateCreate;


/* NOTE - This test class does not test the EducationLineController endpoints! */
@WebMvcTest(EducationLineController.class)
@Import(EducationLineDirectory.class)
@DisableSecurity

class EducationLineControllerTest {


	

	@MockitoBean
	private EducationLineDirectory educationLineDirectory;


    private EducationLineDirectory directory;
    private EducationLineController controller;
    private UUID id;
    private EducationLineResponse dto;

    private static EducationLineResponse response(UUID id, boolean active) {
        return new EducationLineResponse(
                id,
                "Math",
                1,
                2,
                3,
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                Instant.parse("2026-03-16T10:00:00Z"),
                active);
    }

    @Nested
    class ControllerEndpointsSuccessTests {

		@Autowired
    	private MockMvc mockMvc;

		private static final String BASE_URL = "/internal/educationlines";


        @BeforeEach
        void setup() {
            id = UUID.randomUUID();
            dto = response(id, true);
            directory = Mockito.mock(EducationLineDirectory.class);
            controller = new EducationLineController(directory);
        }

        @Test
        void createEducationLine_returns_created_status() {
            when(directory.createEducationLine(Mockito.any(CreateEducationLineCmd.class))).thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.createEducationLine(
                    new CreateEducationLineCmd("Math", 1, 2, 3,
                            UUID.fromString("11111111-1111-1111-1111-111111111111")));
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        void createEducationLine_returns_expected_response() {
            when(directory.createEducationLine(Mockito.any(CreateEducationLineCmd.class))).thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.createEducationLine(
                    new CreateEducationLineCmd("Math", 1, 2, 3,
                            UUID.fromString("11111111-1111-1111-1111-111111111111")));
            assertSame(dto, response.getBody());
        }

        @Test
        void findAll_returns_ok_status() {
            when(directory.findAll()).thenReturn(List.of(dto));
            ResponseEntity<List<EducationLineResponse>> response = controller.findAll();
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void findAll_returns_expected_response() {
            when(directory.findAll()).thenReturn(List.of(dto));
            ResponseEntity<List<EducationLineResponse>> response = controller.findAll();
            assertEquals(1, response.getBody().size());
        }

        @Test
        void findById_returns_ok_status() {
            when(directory.findById(id)).thenReturn(Optional.of(dto));
            ResponseEntity<EducationLineResponse> response = controller.findById(id);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void findById_returns_expected_response() {
            when(directory.findById(id)).thenReturn(Optional.of(dto));
            ResponseEntity<EducationLineResponse> response = controller.findById(id);
            assertSame(dto, response.getBody());
        }

		@Test
		void findByEducationRef_shouldReturn() throws Exception {
			when(directory.findByEducationRef(id)).thenReturn(List.of(dto));
			mockMvc.perform(get(BASE_URL + "/{id}/education-ref", id)
					.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
		}

        @Test
        void updateName_returns_ok_status() {
            when(directory.updateEducationLineName(Mockito.eq(id), Mockito.any(UpdateEducationLineNameCmd.class)))
                    .thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.updateName(id,
                    new UpdateEducationLineNameCmd("Physics"));
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void updateName_returns_expected_response() {
            when(directory.updateEducationLineName(Mockito.eq(id), Mockito.any(UpdateEducationLineNameCmd.class)))
                    .thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.updateName(id,
                    new UpdateEducationLineNameCmd("Physics"));
            assertSame(dto, response.getBody());
        }

        @Test
        void updateDuration_returns_ok_status() {
            when(directory.updateEducationLineDuration(Mockito.eq(id),
                    Mockito.any(UpdateEducationLineDurationCmd.class))).thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.updateDuration(id,
                    new UpdateEducationLineDurationCmd(1, 0, 1));
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void updateDuration_returns_expected_response() {
            when(directory.updateEducationLineDuration(Mockito.eq(id),
                    Mockito.any(UpdateEducationLineDurationCmd.class))).thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.updateDuration(id,
                    new UpdateEducationLineDurationCmd(1, 0, 1));
            assertSame(dto, response.getBody());
        }

        @Test
        void deactivate_returns_ok_status() {
            when(directory.deactivateEducationLine(id)).thenReturn(response(id, false));
            ResponseEntity<EducationLineResponse> response = controller.deactivate(id);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void deactivate_returns_expected_response() {
            when(directory.deactivateEducationLine(id)).thenReturn(response(id, false));
            ResponseEntity<EducationLineResponse> response = controller.deactivate(id);
            assertEquals(false, response.getBody().isActive());
        }

        @Test
        void activate_returns_ok_status() {
            when(directory.activateEducationLine(id)).thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.activate(id);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void activate_returns_expected_response() {
            when(directory.activateEducationLine(id)).thenReturn(dto);
            ResponseEntity<EducationLineResponse> response = controller.activate(id);
            assertSame(dto, response.getBody());
        }
    }

    @Nested
    class ControllerEndpointsFailureTests {

        @BeforeEach
        void setup() {
            id = UUID.randomUUID();
            dto = response(id, true);
            directory = Mockito.mock(EducationLineDirectory.class);
            controller = new EducationLineController(directory);
        }

        @Test
        void createEducationLine_whenServiceReturnsNull_returns_created_status_with_null_body() {
            when(directory.createEducationLine(Mockito.any(CreateEducationLineCmd.class))).thenReturn(null);
            ResponseEntity<EducationLineResponse> response = controller.createEducationLine(
                    new CreateEducationLineCmd("Math", 1, 2, 3,
                            UUID.fromString("11111111-1111-1111-1111-111111111111")));
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(null, response.getBody());
        }

        @Test
        void findAll_whenServiceReturnsEmptyList_returns_ok_status_with_empty_body() {
            when(directory.findAll()).thenReturn(List.of());
            ResponseEntity<List<EducationLineResponse>> response = controller.findAll();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(0, response.getBody().size());
        }

        @Test
        void findById_whenNotFound_returns_not_found_status() {
            when(directory.findById(id)).thenReturn(Optional.empty());
            ResponseEntity<EducationLineResponse> response = controller.findById(id);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void findById_whenNotFound_returns_empty_body() {
            when(directory.findById(id)).thenReturn(Optional.empty());
            ResponseEntity<EducationLineResponse> response = controller.findById(id);
            assertEquals(null, response.getBody());
        }

        @Test
        void updateName_whenServiceReturnsNull_returns_ok_status_with_null_body() {
            when(directory.updateEducationLineName(Mockito.eq(id), Mockito.any(UpdateEducationLineNameCmd.class)))
                    .thenReturn(null);
            ResponseEntity<EducationLineResponse> response = controller.updateName(id,
                    new UpdateEducationLineNameCmd("Physics"));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(null, response.getBody());
        }

        @Test
        void updateDuration_whenServiceReturnsNull_returns_ok_status_with_null_body() {
            when(directory.updateEducationLineDuration(Mockito.eq(id),
                    Mockito.any(UpdateEducationLineDurationCmd.class))).thenReturn(null);
            ResponseEntity<EducationLineResponse> response = controller.updateDuration(id,
                    new UpdateEducationLineDurationCmd(1, 0, 1));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(null, response.getBody());
        }

        @Test
        void deactivate_whenServiceReturnsNull_returns_ok_status_with_null_body() {
            when(directory.deactivateEducationLine(id)).thenReturn(null);
            ResponseEntity<EducationLineResponse> response = controller.deactivate(id);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(null, response.getBody());
        }

        @Test
        void activate_whenServiceReturnsNull_returns_ok_status_with_null_body() {
            when(directory.activateEducationLine(id)).thenReturn(null);
            ResponseEntity<EducationLineResponse> response = controller.activate(id);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(null, response.getBody());
        }
    }

    @Nested
    class CompensateSuccessTest {
        @BeforeEach
        void setup() {
            id = UUID.randomUUID();
            directory = Mockito.mock(EducationLineDirectory.class);
            controller = new EducationLineController(directory);

        }

        @Test
        void compensate_WhenServiceReturnsResult_ShouldReturn200() {

            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            ResponseCompensated expected = new ResponseCompensated(
                    SagaOutcome.COMPENSATED,
                    true);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(expected);

            ResponseEntity<ResponseCompensated> response = controller.compensate(id, cmd);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(expected, response.getBody());
        }

        @Test
        void compensate_WhenServiceReturnsNull_ShouldReturn204() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(null);

            ResponseEntity<ResponseCompensated> response = controller.compensate(id, cmd);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        void compensateActivate_WhenServiceReturnsResult_ShouldReturn200() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            ResponseCompensated expected = new ResponseCompensated(
                    SagaOutcome.COMPENSATED,
                    true);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(expected);

            ResponseEntity<ResponseCompensated> response = controller.compensateActivate(id, cmd);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(expected, response.getBody());
        }

        @Test
        void compensateDeactivate_WhenServiceReturnsResult_ShouldReturn200() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            ResponseCompensated expected = new ResponseCompensated(
                    SagaOutcome.COMPENSATED,
                    true);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(expected);

            ResponseEntity<ResponseCompensated> response = controller.compensateDeactivate(id, cmd);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(expected, response.getBody());
        }

        @Test
        void compensateName_WhenServiceReturnsResult_ShouldReturn200() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            UpdateEducationLineNameCmd nameReq = new UpdateEducationLineNameCmd("New Name");
            ResponseCompensated expected = new ResponseCompensated(
                    SagaOutcome.COMPENSATED,
                    true);

            when(directory.compensateName(id, cmd.clazz(), cmd.sagaState(), nameReq)).thenReturn(expected);

            ResponseEntity<ResponseCompensated> response = controller.compensateName(id, cmd, nameReq);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(expected, response.getBody());
        }

        @Test
        void compensateDuration_WhenServiceReturnsResult_ShouldReturn200() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            UpdateEducationLineDurationCmd durationReq = new UpdateEducationLineDurationCmd(2, 6, 0);
            ResponseCompensated expected = new ResponseCompensated(
                    SagaOutcome.COMPENSATED,
                    true);

            when(directory.compensateDuration(id, cmd.clazz(), cmd.sagaState(), durationReq)).thenReturn(expected);

            ResponseEntity<ResponseCompensated> response = controller.compensateDuration(id, cmd, durationReq);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(expected, response.getBody());
        }
    }
    
    @Nested
    class CompensateFailureTests {
        @BeforeEach
        void setup() {
            id = UUID.randomUUID();
            directory = Mockito.mock(EducationLineDirectory.class);
            controller = new EducationLineController(directory);
        }

        @Test
        void compensate_WhenServiceReturnsNull_ShouldReturn204() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(null);

            ResponseEntity<ResponseCompensated> response = controller.compensate(id, cmd);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        void compensateActivate_WhenServiceReturnsNull_ShouldReturn204() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(null);

            ResponseEntity<ResponseCompensated> response = controller.compensateActivate(id, cmd);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        void compensateDeactivate_WhenServiceReturnsNull_ShouldReturn204() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);

            when(directory.compensate(id, cmd.clazz(), cmd.sagaState())).thenReturn(null);

            ResponseEntity<ResponseCompensated> response = controller.compensateDeactivate(id, cmd);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        void compensateName_WhenServiceReturnsNull_ShouldReturn204() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            UpdateEducationLineNameCmd nameReq = new UpdateEducationLineNameCmd("New Name");

            when(directory.compensateName(id, cmd.clazz(), cmd.sagaState(), nameReq)).thenReturn(null);

            ResponseEntity<ResponseCompensated> response = controller.compensateName(id, cmd, nameReq);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        void compensateDuration_WhenServiceReturnsNull_ShouldReturn204() {
            PayloadCompensateCreate cmd = new PayloadCompensateCreate(
                    String.class,
                    SagaOutcome.COMPENSATE);
            UpdateEducationLineDurationCmd durationReq = new UpdateEducationLineDurationCmd(2, 6, 0);

            when(directory.compensateDuration(id, cmd.clazz(), cmd.sagaState(), durationReq)).thenReturn(null);

            ResponseEntity<ResponseCompensated> response = controller.compensateDuration(id, cmd, durationReq);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }
}

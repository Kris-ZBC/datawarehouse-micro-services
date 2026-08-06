package local.sop.datawarehouse.anonymize.saga.application.interfaceweb;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import java.util.Map;

import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.datawarehouse.anonymize.saga.application.api.AnonymizeSagaDirectory;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.AnonymizeResponse;
import local.sop.datawarehouse.anonymize.saga.application.api.dto.CreateAnonymizeCmd;
import local.sop.datawarehouse.anonymize.saga.application.interfaceweb.AnonymizeSagaController;

@WebMvcTest(AnonymizeSagaController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AnonymizeSagaControllerTest {
    private static final String URL = "/internal/saga/anonymizations";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnonymizeSagaDirectory anonymizeDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID anonymizationId;
    private AnonymizeResponse anonymizeResponse;
    private CreateAnonymizeCmd createAnonymizeCmd;
    private UUID personRef;

    @BeforeEach
    void setUp() {
        personRef = UUID.randomUUID();
        anonymizationId = UUID.randomUUID();
        anonymizeResponse = new AnonymizeResponse(anonymizationId, personRef);
        createAnonymizeCmd = new CreateAnonymizeCmd(
                personRef,
                UUID.randomUUID(),
                ActorType.USER,
                Severity.INFO,
                "originSystem",
                "originService",
                "originComponent",
                "data",
                "description"
        );
    }



    /* =========================================================== *
     *  Happy path
     * =========================================================== */


    @Nested
    class HappyPath {

        @BeforeEach
        void directoryReturnsOk() {
            when(anonymizeDirectory.create(any())).thenReturn(anonymizeResponse);
        }

        @Test
        void create_ShouldReturn201_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createAnonymizeCmd)))
                    .andExpect(status().isCreated());
        }

        @Test
        void create_shouldReturnAnonymizationId_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createAnonymizeCmd)))
                    .andExpect(jsonPath("$.anonymizationId").value(anonymizationId.toString()));
        }

        @Test
        void create_shouldReturnPersonRef_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createAnonymizeCmd)))
                    .andExpect(jsonPath("$.personRef").value(personRef.toString()));
        }

        @Test
        void create_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createAnonymizeCmd)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void create_shouldDelegateToDirectory_whenRequestIsValid() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createAnonymizeCmd)));

            verify(anonymizeDirectory).create(any(CreateAnonymizeCmd.class));
        }
    }
    /* =========================================================== *
     *  Validation — @Valid on @RequestBody
     * =========================================================== */

    @Nested
    class Validation {

        @Test
        void create_shouldReturn400_whenBodyIsMissing() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenBodyIsMalformedJson() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not valid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenPersonRefIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withPersonRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenActorRefIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withActorRef(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenActorTypeIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withActorType(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenSeverityIsNull() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withSeverity(null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenOriginSystemIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withOriginSystem("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenOriginServiceIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withOriginService("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenOriginComponentIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withOriginComponent("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDataIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withData("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturn400_whenDescriptionIsBlank() throws Exception {
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(withDescription("   "))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void create_shouldReturnProblemDetailWithErrorsMap_whenValidationFails() throws Exception {
            mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withActorRef(null)))
            ).andExpect(jsonPath("$.errors").isMap());
        }

        @Test
        void create_shouldNeverCallDirectory_whenValidationFails() throws Exception {
            mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withActorRef(null)))
            );

            verify(anonymizeDirectory, never()).create(any());
        }

    }


	/* =========================================================== *
     *  Service / domain layer failures — RFC 7807 ProblemDetail
     * =========================================================== */
	@Nested
	class ServiceFailures {

		@Test 
		void create_shouldReturn409_whenDirectoryThrowsConflictException() throws Exception {
			when(anonymizeDirectory.create(any()))
				.thenThrow(new ConflictException("anonymize.notcreated",
					Map.of("object", "anonymization")));

			mockMvc.perform(post(URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAnonymizeCmd)))
				.andExpect(status().isConflict());
		}

        @Test
        void create_shouldReturnProblemTypeConflict_whenDirectoryThrowsConflictException() throws Exception {
            when(anonymizeDirectory.create(any())).thenThrow(new ConflictException("anonymize.notcreated", Map.of("object", "anonymization")));

            mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAnonymizeCmd))
            ).andExpect(jsonPath("$.type").value("about:blank#conflict"));
        }

        @Test
        void create_shouldReturnProblemKey_whenDirectoryThrowsConflictException() throws Exception {
            when(anonymizeDirectory.create(any())).thenThrow(new ConflictException("anonymize.notcreated", Map.of("object", "anonymization")));

            mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAnonymizeCmd))
            ).andExpect(jsonPath("$.key").value("anonymize.notcreated"));
        }

		@Test
		void create_shouldReturn409_whenDirectoryThrowsConflictExceptionForAuditlog() throws Exception {
			when(anonymizeDirectory.create(any()))
				.thenThrow(new ConflictException("auditlog.notcreated",
					Map.of("object", "auditlog")));
			
			mockMvc.perform(post(URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(createAnonymizeCmd)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.key").value("auditlog.notcreated"));
		}

		@Test
		void create_shouldReturn500_whenDirectoryThrowsUnexpectedRuntimeException() throws Exception {
			when(anonymizeDirectory.create(any()))
				.thenThrow(new RuntimeException("Unexpected failure"));
			
			mockMvc.perform(post(URL)
						.contentType(MediaType.APPLICATION_JSON)
				    	.content(objectMapper.writeValueAsString(createAnonymizeCmd)))
					.andExpect(status().isInternalServerError());
		}
		
        @Test
        void create_shouldProblemTypeReturnInternalError_onUnexpectedException() throws Exception {
            when(anonymizeDirectory.create(any())).thenThrow(new RuntimeException("unexpected failure"));

            mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAnonymizeCmd))
            ).andExpect(jsonPath("$.type").value("about:blank#internal_error"));
        }
	}





    /* =========================================================== *
     *  Builder helpers — produce clean variants of validCmd
     * =========================================================== */


    private CreateAnonymizeCmd withPersonRef(UUID personRef) {
        return new CreateAnonymizeCmd(personRef,
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withActorRef(UUID actorRef) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                actorRef, createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withActorType(ActorType actorType) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), actorType, createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withSeverity(Severity severity) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), severity,
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withOriginSystem(String originSystem) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                originSystem, createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withOriginService(String originService) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), originService, createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withOriginComponent(String originComponent) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), originComponent,
                createAnonymizeCmd.data(), createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withData(String data) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                data, createAnonymizeCmd.description());
    }

    private CreateAnonymizeCmd withDescription(String description) {
        return new CreateAnonymizeCmd(createAnonymizeCmd.personRef(),
                createAnonymizeCmd.actorRef(), createAnonymizeCmd.actorType(), createAnonymizeCmd.severity(),
                createAnonymizeCmd.originSystem(), createAnonymizeCmd.originService(), createAnonymizeCmd.originComponent(),
                createAnonymizeCmd.data(), description);
    }


}

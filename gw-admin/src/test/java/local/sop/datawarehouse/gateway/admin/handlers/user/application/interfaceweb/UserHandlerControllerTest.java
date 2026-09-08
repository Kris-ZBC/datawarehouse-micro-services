package local.sop.datawarehouse.gateway.admin.handlers.user.application.interfaceweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.security.context.RequestPrincipalContext;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.datawarehouse.sharedlib.enums.ConsentStatus;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.request.RegisterInstructorRequest;
import local.sop.datawarehouse.gateway.admin.handlers.user.api.dto.response.CreatedUserResponse;
import local.sop.datawarehouse.gateway.admin.handlers.user.application.service.UserHandlerService;

/**
 * NOTE on async testing: registerInstructor() returns a
 * CompletableFuture, so Spring MVC dispatches it asynchronously — a
 * single mockMvc.perform() only gets you as far as "the async request
 * started." Getting the actual HTTP response requires the standard
 * two-step dance: perform() + andExpect(request().asyncStarted()) to
 * capture the MvcResult, then a second perform(asyncDispatch(result))
 * to get the real status/body. performAsync() below wraps that.
 *
 * NOTE on RequestPrincipalContext: @WebMvcTest auto-detects
 * WebMvcConfigurer beans in its scanned slice, so WebMvcConfig — and
 * with it RoleAuthorizationInterceptor — is wired into this test
 * context too, exactly as it would be in production. @DisableSecurity
 * only turns off Spring Security's own auto-config; it has no bearing
 * on this interceptor, which isn't part of Spring Security. In
 * production, GatewaySessionContextFilter/InternalRoleContextFilter
 * populate RequestPrincipalContext before the controller ever runs —
 * neither filter is part of this slice, so setUp() stubs that same
 * precondition directly (a session already resolved to an allowed
 * role) rather than disabling or mocking the interceptor itself. This
 * keeps the real authorization wiring exercised, same philosophy as
 * ServiceFailures below testing the real exception-handling wiring.
 */
@WebMvcTest(UserHandlerController.class)
@Import(EndpointExceptionHandler.class)
@DisableSecurity
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserHandlerControllerTest {

    private static final String URL = "/api/v1/admin/users/instructors";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserHandlerService userHandlerService;

    private UUID createdId;
    private RegisterInstructorRequest requestBody;
    private CreatedUserResponse response;

    @BeforeEach
    void setUp() {
        createdId = UUID.randomUUID();

        requestBody = new RegisterInstructorRequest(
                "Daniel",
                "S",
                "dani423j@zbc.dk",
                UUID.randomUUID(),
                List.of(new RegisterInstructorRequest.PhoneNumber("MOBILE", "+4512345678")),
                "dani423j",
                List.of(new RegisterInstructorRequest.ConsentStatement(UUID.randomUUID(), ConsentStatus.ACTIVE)));

        response = new CreatedUserResponse(createdId);

        // Simulates GatewaySessionContextFilter having already resolved
        // a valid TECHUSER session — see class javadoc.
        RequestPrincipalContext.set(new RequestPrincipalContext.Principal(
                UUID.randomUUID(), UUID.randomUUID(), "techuser", "TECHUSER"));
    }

    @AfterEach
    void tearDown() {
        // MUST clear — see RequestPrincipalContext's own contract:
        // virtual threads get reused, so a leaked ThreadLocal here
        // leaks into whichever test method's carrier thread runs next
        // (this class reuses one context across all @Test methods per
        // @DirtiesContext(AFTER_CLASS)).
        RequestPrincipalContext.clear();
    }

    private MvcResult startAsyncRequest() throws Exception {
        return mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(request().asyncStarted())
                .andReturn();
    }

    /*
     * =========================================================== *
     * Happy path
     * ===========================================================
     */

    @Nested
    class HappyPath {

        @BeforeEach
        void serviceReturnsOk() {
            when(userHandlerService.registerInstructor(any()))
                    .thenReturn(CompletableFuture.completedFuture(response));
        }

        @Test
        void create_shouldReturn201_whenRequestIsValid() throws Exception {
            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isCreated());
        }

        @Test
        void create_shouldReturnLocationHeader_whenRequestIsValid() throws Exception {
            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(header().string("Location", "/api/v1/admin/users/instructors/" + createdId));
        }

        @Test
        void create_shouldReturnCreatedId_whenRequestIsValid() throws Exception {
            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(jsonPath("$.id").value(createdId.toString()));
        }

        @Test
        void create_shouldReturnApplicationJson_whenRequestIsValid() throws Exception {
            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        void create_shouldDelegateToService_whenRequestIsValid() throws Exception {
            MvcResult started = startAsyncRequest();
            mockMvc.perform(asyncDispatch(started));

            verify(userHandlerService).registerInstructor(requestBody);
        }
    }

    /*
     * =========================================================== *
     * Validation — @Valid on @RequestBody
     * ===========================================================
     */

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
        void create_shouldReturn400_whenRequiredFieldMissing() throws Exception {
            RegisterInstructorRequest invalid = new RegisterInstructorRequest(
                    null, "S", "dani423j@zbc.dk", UUID.randomUUID(), List.of(), "dani423j", List.of());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    /*
     * =========================================================== *
     * Service failures — the controller no longer maps these itself;
     * this exercises the REAL end-to-end path: mocked service completes
     * exceptionally with a raw exception -> the controller's own
     * .thenApply() wraps it in CompletionException (guaranteed, per
     * CompletableFuture's own semantics, independent of anything Spring
     * does internally) -> real Spring MVC async dispatch -> the real
     * EndpointExceptionHandler bean (via @Import, not mocked) ->
     * ProblemDetail. This is not testing our own error-mapping logic in
     * isolation — it's proving the actual wiring between an async
     * controller, CompletableFuture wrapping, and the shared advice
     * genuinely connects, which was the open question this test exists
     * to answer.
     * ===========================================================
     */

    @Nested
    class ServiceFailures {

        private CompletableFuture<CreatedUserResponse> failedWith(Throwable ex) {
            CompletableFuture<CreatedUserResponse> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }

        @Test
        void create_shouldReturn504ProblemDetail_whenSagaCallTimesOut() throws Exception {
            // Raw TimeoutException, unwrapped — matches exactly what
            // UserHandlerService's real orTimeout()/whenComplete() chain
            // produces internally. No manual CompletionException wrapping
            // here; that happens for real, inside the real controller.
            when(userHandlerService.registerInstructor(any()))
                    .thenReturn(failedWith(new TimeoutException()));

            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isGatewayTimeout())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(504))
                    .andExpect(jsonPath("$.key").value("gateway.timeout"));
        }

        @Test
        void create_shouldReturn503ProblemDetail_whenSagaIsUnreachable() throws Exception {
            when(userHandlerService.registerInstructor(any()))
                    .thenReturn(failedWith(new ResourceAccessException("connection refused")));

            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(503))
                    .andExpect(jsonPath("$.key").value("downstream.unavailable"));
        }

        @Test
        void create_shouldPassThroughSagaConflictAsProblemDetail() throws Exception {
            when(userHandlerService.registerInstructor(any()))
                    .thenReturn(failedWith(HttpClientErrorException.create(
                            HttpStatus.CONFLICT, "Conflict", new HttpHeaders(), new byte[0], null)));

            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.key").value("downstream.client.error"));
        }

        @Test
        void create_shouldReturn502ProblemDetail_whenSagaReportsServerError() throws Exception {
            when(userHandlerService.registerInstructor(any()))
                    .thenReturn(failedWith(HttpServerErrorException.create(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", new HttpHeaders(), new byte[0], null)));

            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isBadGateway())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(502))
                    .andExpect(jsonPath("$.key").value("downstream.server.error"));
        }

        @Test
        void create_shouldReturn500ProblemDetail_whenUnexpectedExceptionOccurs() throws Exception {
            when(userHandlerService.registerInstructor(any()))
                    .thenReturn(failedWith(new RuntimeException("boom")));

            MvcResult started = startAsyncRequest();

            mockMvc.perform(asyncDispatch(started))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.key").value("server.error"));
        }
    }
}
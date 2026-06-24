package local.sop.sopinfo.interfaceadapters.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.ApplicationContext;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
                "internal.mtls.allowed-callers=",
                "security.enabled=false",
                "spring.autoconfigure.exclude=" +
                   "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                   "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration," 
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EndpointExceptionHandlerIntegrationTest {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EndpointExceptionHandlerIntegrationTest.class);

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper om;

       
        @Test
        void debugBeanRemoval() {
                ApplicationContext context = mvc.getDispatcherServlet().getWebApplicationContext();
                Map<String, SecurityFilterChain> chains = context.getBeansOfType(SecurityFilterChain.class);
                log.debug("FINAL SecurityFilterChain beans: " + chains.keySet());

                // Skal kun vise: [testSecurityFilterChain]
                chains.forEach((name, chain) -> {
                        log.debug(name + " -> " + chain.getClass().getName());
                });
        }

        @Test
        void domainConflict409() throws Exception {
                mvc.perform(get("/__test/domain-conflict").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.type", is("about:blank#conflict")))
                                .andExpect(jsonPath("$.detail", is("Email is already in use")))
                                .andExpect(jsonPath("$.key", is("email.exists")))
                                .andExpect(jsonPath("$.args.field", is("email")));
        }

        @Test
        void bodyValidation400() throws Exception {
                var badBody = Map.of("name", ""); // @NotBlank fails
                mvc.perform(post("/__test/body").header("Accept-Language", "en-US,en;q=0.9")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(badBody)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type", is("about:blank#validation")))
                                .andExpect(jsonPath("$.detail", is("One or more fields are invalid")))
                                .andExpect(jsonPath("$.errors.name", not(blankOrNullString())));
        }

        @Test
        void malformedJson400() throws Exception {
                // invalid JSON -> HttpMessageNotReadableException
                mvc.perform(post("/__test/body").header("Accept-Language", "en-US,en;q=0.9")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid json"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type", is("about:blank#malformed_json")))
                                .andExpect(jsonPath("$.detail", is("Malformed JSON request body"))); // your handler
                                                                                                     // returns a key;
                                                                                                     // adapt if needed
        }

        @Test
        void missingParam400() throws Exception {
                mvc.perform(get("/__test/needs-q").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type", is("about:blank#missing_parameter")))
                                .andExpect(jsonPath("$.detail", containsString("Request parameter is required")));
        }

        @Test
        void typeMismatch400() throws Exception {
                mvc.perform(get("/__test/type?id=abc").header("Accept-Language", "en-US,en;q=0.9"))
                                .andDo(result -> {
                                        log.debug("Status: " + result.getResponse().getStatus());
                                        log.debug("Content: " + result.getResponse().getContentAsString());
        })
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type", is("about:blank#type_mismatch")))
                                .andExpect(jsonPath("$.detail", containsString("Request parameter is invalid")))
                                .andExpect(jsonPath("$.args.param", is("id")));
        }

        @Test
        void keyParamInvalid400() throws Exception {
                mvc.perform(get("/__test/type/abc").header("Accept-Language", "en-US,en;q=0.9"))
                                .andDo(result -> {
                                        log.debug("Status: " + result.getResponse().getStatus());
                                        log.debug("Content: " + result.getResponse().getContentAsString());
                                })
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type", is("about:blank#type_mismatch")))
                                .andExpect(jsonPath("$.detail", containsString("Request parameter is invalid")))
                                .andExpect(jsonPath("$.args.param", is("id")));
        }

        @Test
        void paramConstraint400() throws Exception {
                mvc.perform(get("/__test/age?age=10").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.type", is("about:blank#validation")))
                                .andExpect(jsonPath("$.detail", is("Request parameter validation failed")));
        }

        @Test
        void notFound404() throws Exception {
                mvc.perform(get("/__test/not-found").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.type", is("about:blank#not_found")))
                                .andExpect(jsonPath("$.key", is("test.not.found")))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.args.id").value(123))
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/not-found"));
        }

        @Test
        void validationDomainException400() throws Exception {
                mvc.perform(get("/__test/validation").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.type").value("about:blank#validation"))
                                .andExpect(jsonPath("$.key").value("test.validation.failed"))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/validation"));
        }

        @Test
        void notFoundDomainException400() throws Exception {
                mvc.perform(get("/__test/not-found").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.type").value("about:blank#not_found"))
                                .andExpect(jsonPath("$.key").value("test.not.found"))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/not-found"));
        }

        @Test
        void invariantDomainException400() throws Exception {
                mvc.perform(get("/__test/invariant").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.status").value(422))
                                .andExpect(jsonPath("$.type").value("about:blank#invariant"))
                                .andExpect(jsonPath("$.key").value("test.invariant"))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/invariant"));
        }

        @Test
        void preconditionDomainException400() throws Exception {
                mvc.perform(get("/__test/precondition").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isPreconditionFailed())
                                .andExpect(jsonPath("$.status").value(412))
                                .andExpect(jsonPath("$.type").value("about:blank#precondition"))
                                .andExpect(jsonPath("$.key").value("test.precondition"))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/precondition"));
        }

        @Test
        void concurrencyDomainException400() throws Exception {
                mvc.perform(get("/__test/concurrency").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.type").value("about:blank#concurrency"))
                                .andExpect(jsonPath("$.key").value("test.concurrency"))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/concurrency"));
        }

        @Test
        void conflictDomainException409() throws Exception {
                mvc.perform(get("/__test/conflict").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.type").value("about:blank#conflict"))
                                .andExpect(jsonPath("$.key").value("test.conflict"))
                                .andExpect(jsonPath("$.args").exists())
                                .andExpect(jsonPath("$.args.resource").value("user"))
                                .andExpect(jsonPath("$.detail").exists())
                                .andExpect(jsonPath("$.instance").value("/__test/conflict"));
        }

        @Test
        void whenPathVariableTypeMismatch_thenHandleBindException() throws Exception {
        mvc.perform(get("/__test/type/abc"))  // Long expected, got String
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:blank#type_mismatch"));
        }

        @Test
        void whenFormDataInvalid_thenHandleBindException() throws Exception {
        mvc.perform(post("/__test/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("age", "not-a-number")  // Type mismatch -> BindException
                .param("email", "invalid-email")  // Validation error
                .param("name", ""))  // Validation error
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:blank#type_mismatch")) 
                .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        void dataIntegrity_unique409_postgres() throws Exception {
                mvc.perform(get("/__test/db/unique").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.type", is("about:blank#unique_violation")))
                                .andExpect(jsonPath("$.detail", is("Unique constraint violated")));
        }

        @Test
        void dataIntegrity_unique409_sqlserver() throws Exception {
                mvc.perform(get("/__test/db/sqlserver-unique").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.type", is("about:blank#unique_violation")))
                                .andExpect(jsonPath("$.detail", is("Unique constraint violated")));
        }

        @Test
        void dataIntegrity_conflict409_fallback() throws Exception {
                mvc.perform(get("/__test/db/conflict").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.type", is("about:blank#conflict")))
                                .andExpect(jsonPath("$.detail", is("Conflict with existing data")));
        }

        @Test
        void generic500() throws Exception {
                mvc.perform(get("/__test/boom").header("Accept-Language", "en-US,en;q=0.9"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.type", is("about:blank#internal_error")))
                                .andExpect(jsonPath("$.detail", is("Internal server error")));
        }

}

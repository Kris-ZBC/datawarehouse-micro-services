package local.sop.sopinfo.institution.interfaceweb;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import local.sop.sopinfo.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.sopinfo.institution.application.api.InstitutionDirectory;
import local.sop.sopinfo.institution.application.api.dto.InstitutionQuery;
import local.sop.sopinfo.institution.application.api.dto.InstitutionResponse;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;

@WebMvcTest(controllers = InternalInstitutionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ EndpointExceptionHandler.class, InstitutionControllerTest.TestI18Config.class })
public class InstitutionControllerTest {

    @MockitoBean
    InstitutionDirectory directory;

    @Autowired
    MockMvc mvc;

    @TestConfiguration
    static class TestI18Config {
        @Bean
        ResourceBundleMessageSource messageSource() {
            var ms = new ResourceBundleMessageSource();
            ms.setBasename("i18n/messages");
            ms.setDefaultEncoding("UTF-8");
            return ms;
        }
    }

    @Test
    void getByPath_returns200() throws Exception {
        var id = UUID.randomUUID();
        var view = new InstitutionResponse(id, "Test Institution", "Test Address");
        
        when(directory.findById(eq(new InstitutionQuery(id)))).thenReturn(view);

        mvc.perform(get("/internal/institutions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Test Institution"))
                .andExpect(jsonPath("$.address").value("Test Address"));
    }

    @Test
    void getByPath_notFound_returns404() throws Exception {
        var id = UUID.randomUUID();
        when(directory.findById(any(InstitutionQuery.class)))
                .thenThrow(new NotFoundException("institution.notFound", Map.of("id", id)));

        mvc.perform(get("/internal/institutions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testResponseEntity_returns200() throws Exception {
        mvc.perform(get("/internal/institutions/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("PONG!"));
    }
}
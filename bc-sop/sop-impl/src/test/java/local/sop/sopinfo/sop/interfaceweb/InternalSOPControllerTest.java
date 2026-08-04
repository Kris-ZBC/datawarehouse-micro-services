package local.sop.sopinfo.sop.interfaceweb;

import java.util.UUID;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.sopinfo.sop.application.api.SOPDirectory;
import local.sop.sopinfo.sop.application.api.dto.SOPQuery;
import local.sop.sopinfo.sop.application.api.dto.SopResponse;

@WebMvcTest(controllers = InternalSOPController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ EndpointExceptionHandler.class, InternalSOPControllerTest.TestI18Config.class })
public class InternalSOPControllerTest {

    @MockitoBean 
    SOPDirectory directory;
    
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
        var view = new SopResponse(id, "Ringsted Data/IT", "P7-10", "IT Support");

        when(directory.findById(Mockito.any(SOPQuery.class))).thenReturn(Optional.of(view));

        mvc.perform(get("/internal/sop/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Ringsted Data/IT"))
            .andExpect(jsonPath("$.address").value("P7-10"))
            .andExpect(jsonPath("$.education").value("IT Support"));
    }

    @Test
    void getByPath_notFound_returns404() throws Exception {
        when(directory.findById(Mockito.any(SOPQuery.class)))
            .thenReturn(Optional.empty());

        mvc.perform(get("/internal/sop/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
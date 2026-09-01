package local.sop.datawarehouse.organisation.interfaceweb;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import local.sop.common.libs.infrastructure.security.DisableSecurity;
import local.sop.common.libs.infrastructure.web.exception.EndpointExceptionHandler;
import local.sop.datawarehouse.organisation.application.api.OrganisationDirectory;
import local.sop.datawarehouse.organisation.application.api.dto.OrganisationResponse;

@WebMvcTest(controllers =InternalOrganisationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({EndpointExceptionHandler.class})
@DisableSecurity
public class InternalOrganisationControllerTest {

    @MockitoBean 
    OrganisationDirectory directory;
    @Autowired MockMvc mvc;

    @Test
    void getByPath_returns200() throws Exception {
        var id = UUID.randomUUID();
        var view = new OrganisationResponse(id, "Test Organisation", "12345678");
        when(directory.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(view));

        mvc.perform(get("/internal/organisations/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("Test Organisation"))
        .andExpect(jsonPath("$.cvr").value("12345678"));
    }

    @Test
    void getByPath_notFound_returns404() throws Exception {/* << test 204 >> (så vis koden ovenfor her fejler skal vi teste den) */
        when(directory.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());

        mvc.perform(get("/internal/organisations/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound());
    }
    
    @Test
    void getByPath_notFound_returns400() throws Exception {/* << test 204 >> (så vis koden ovenfor her fejler skal vi teste den) */
        when(directory.findById(Mockito.any(UUID.class))).thenThrow(new IllegalArgumentException("Invalid UUID format"));

        mvc.perform(get("/internal/organisations/{id}", "invalid-uuid")) // invalid UUID format
        .andExpect(status().isBadRequest());
    }
}

package local.sop.datawarehouse.institution.application.api.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstitutionDtoTest {

    @Test
    void institutionQuery_holdsId() {
        UUID id = UUID.randomUUID();
        InstitutionQuery query = new InstitutionQuery(id);

        assertEquals(id, query.id());
    }

    @Test
    void institutionResponse_holdsFields() {
        UUID id = UUID.randomUUID();
        InstitutionResponse response =
                new InstitutionResponse(id, "ZBC Ringsted", "Ahorn Alle 5, Ringsted");

        assertEquals(id, response.id());
        assertEquals("ZBC Ringsted", response.name());
        assertEquals("Ahorn Alle 5, Ringsted", response.address());
    }
}

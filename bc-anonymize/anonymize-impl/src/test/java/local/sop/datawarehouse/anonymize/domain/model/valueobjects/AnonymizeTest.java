package local.sop.datawarehouse.anonymize.domain.model.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.anonymize.domain.model.Anonymize;

public class AnonymizeTest {
  @Test
    void create_shouldSetPersonRefAndGenerateId() {
        // given
        UUID uuid = UUID.randomUUID();
        PersonRef personRef = new PersonRef(uuid);

        // when
        Anonymize result = Anonymize.create(personRef);

        // then
        assertNotNull(result);
        assertEquals(personRef, result.getPersonRef());
        assertNotNull(result.getAnonymizationId());
    }

    @Test
    void of_shouldUseProvidedValues() {
        // given
        UUID uuid = UUID.randomUUID();
        PersonRef personRef = new PersonRef(uuid);
        AnonymizeId id = AnonymizeId.newId();

        // when
        Anonymize result = Anonymize.of(id, personRef);

        // then
        assertEquals(id, result.getAnonymizationId());
        assertEquals(personRef, result.getPersonRef());
    }

    @Test
    void create_shouldGenerateDifferentIds() {
        // given
        PersonRef personRef = new PersonRef(UUID.randomUUID());

        // when
        Anonymize a1 = Anonymize.create(personRef);
        Anonymize a2 = Anonymize.create(personRef);

        // then
        assertNotEquals(a1.getAnonymizationId(), a2.getAnonymizationId());
    }
}

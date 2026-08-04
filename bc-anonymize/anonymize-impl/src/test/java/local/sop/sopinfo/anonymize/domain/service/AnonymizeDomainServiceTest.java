package local.sop.sopinfo.anonymize.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.anonymize.domain.model.Anonymize;
import local.sop.sopinfo.anonymize.domain.model.valueobjects.PersonRef;

public class AnonymizeDomainServiceTest {
    @Test
    void create_shouldDelegateToAnonymizeFactory() {
        // given
        AnonymizeDomainService service = new AnonymizeDomainService();
        PersonRef personRef = new PersonRef(UUID.randomUUID());

        // when
        Anonymize result = service.create(personRef);

        // then
        assertNotNull(result);
        assertEquals(personRef, result.getPersonRef());
        assertNotNull(result.getAnonymizationId());
    }
}

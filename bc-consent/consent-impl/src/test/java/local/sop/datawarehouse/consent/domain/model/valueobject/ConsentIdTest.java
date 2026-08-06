package local.sop.datawarehouse.consent.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConsentIdTest {
    @Test
    @DisplayName("Should create new ConsentId with non-null value.")
    void shouldCreateNewConsentId() {
        ConsentId newId = ConsentId.newId();
        assertNotNull(newId);
        assertNotNull(newId.value());
    }

    @Test
    @DisplayName("Should create ConsentId from string.")
    void createConsentIdFromString() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ConsentId consentId = ConsentId.of(raw, "testField");
        assertNotNull(consentId);
        assertNotNull(consentId.value());
    }

    @Test
    @DisplayName("Should create ConsentId from UUID.")
    void createConsentIdFromUUID() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ConsentId consentId = ConsentId.of(raw, "testField");
        assertNotNull(consentId);
        assertNotNull(consentId.value());
    }

    @Test
    @DisplayName("Should create ConsentId from string using fromString method.")
    void createConsentIdFromStringUsingFromString() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ConsentId consentId = ConsentId.fromString(raw, "testField");
        assertNotNull(consentId);
        assertNotNull(consentId.value());
    }
}
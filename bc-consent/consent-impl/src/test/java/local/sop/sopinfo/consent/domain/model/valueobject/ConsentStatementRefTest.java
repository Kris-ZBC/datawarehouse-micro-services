package local.sop.sopinfo.consent.domain.model.valueobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConsentStatementRefTest {
    @Test
    @DisplayName("Should create new ConsentStatementRef with non-null value.")
    void shouldCreateNewConsentStatementRef() {
        ConsentStatementRef newRef = ConsentStatementRef.newId();
        assertNotNull(newRef);
        assertNotNull(newRef.value());
    }

    @Test
    @DisplayName("Should create ConsentStatementRef from string.")
    void createConsentStatementRefFromString() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ConsentStatementRef consentStatementRef = ConsentStatementRef.of(raw, "testField");
        assertNotNull(consentStatementRef);
        assertNotNull(consentStatementRef.value());
    }

    @Test
    @DisplayName("Should create ConsentStatementRef from UUID.")
    void createConsentStatementRefFromUUID() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ConsentStatementRef consentStatementRef = ConsentStatementRef.of(raw, "testField");
        assertNotNull(consentStatementRef);
        assertNotNull(consentStatementRef.value());
    }

    @Test
    @DisplayName("Should create ConsentStatementRef from string using fromString method.")
    void createConsentStatementRefFromStringUsingFromString() {
        String raw = "123e4567-e89b-12d3-a456-426614174000";
        ConsentStatementRef consentStatementRef = ConsentStatementRef.fromString(raw, "testField");
        assertNotNull(consentStatementRef);
        assertNotNull(consentStatementRef.value());
    }
}
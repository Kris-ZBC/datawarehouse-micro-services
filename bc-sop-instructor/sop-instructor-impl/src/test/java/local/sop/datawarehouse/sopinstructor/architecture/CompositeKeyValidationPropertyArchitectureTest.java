package local.sop.datawarehouse.sopinstructor.architecture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(packages = "local.sop.sopinfo.sopinstructor")
class CompositeKeyValidationPropertyArchitectureTest {

    @Test
    void compositekey_validate_enabled_shouldBeSetToTrue_inApplicationProperties() 
            throws Exception {
        Properties props = new Properties();
        try (var stream = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            assertNotNull(stream, "application.properties not found on classpath");
            props.load(stream);
        }

        String value = props.getProperty("compositekey.validate.enabled");
        assertNotNull(value,
            "compositekey.validate.enabled must be set in application.properties " +
            "since this BC uses @ValidateCompositeKey");

        // Accept both plain 'true' and placeholder with default 'true'
        boolean isEnabled = "true".equalsIgnoreCase(value) ||
                            value.contains(":true}");

        assertTrue(isEnabled,
            "compositekey.validate.enabled must default to 'true' in application.properties " +
            "since this BC uses @ValidateCompositeKey. Found: " + value);
    }
}
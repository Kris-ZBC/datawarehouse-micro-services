package local.sop.datawarehouse.auditlog.interfaceadapters.persistence.converters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.Severity;

class SeverityConverterTest {

    private final SeverityConverter converter = new SeverityConverter();

    @Test
    void convertToDatabaseColumn_shouldReturnNull_whenAttributeIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_shouldMapAllKnownValues() {
        assertEquals("debug", converter.convertToDatabaseColumn(Severity.DEBUG));
        assertEquals("info", converter.convertToDatabaseColumn(Severity.INFO));
        assertEquals("low", converter.convertToDatabaseColumn(Severity.WARNING));
        assertEquals("medium", converter.convertToDatabaseColumn(Severity.ERROR));
        assertEquals("high", converter.convertToDatabaseColumn(Severity.FATAL));
    }

    @Test
    void convertToEntityAttribute_shouldReturnNull_whenDbDataIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_shouldMapAllKnownValues_caseInsensitive() {
        assertEquals(Severity.DEBUG, converter.convertToEntityAttribute("debug"));
        assertEquals(Severity.INFO, converter.convertToEntityAttribute("INFO"));
        assertEquals(Severity.WARNING, converter.convertToEntityAttribute("low"));
        assertEquals(Severity.ERROR, converter.convertToEntityAttribute("MEDIUM"));
        assertEquals(Severity.FATAL, converter.convertToEntityAttribute("high"));
    }

    @Test
    void convertToEntityAttribute_shouldThrow_whenUnknownDbValue() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("unknown")
        );

        assertTrue(ex.getMessage().contains("Unknown DB severity value"));
    }
    @Test
    void convertToDatabaseColumn_shouldThrow_whenUnknownSeverity() {
        SeverityConverter converter = new SeverityConverter();

        // Create a fake Severity instance using a mock (works if Severity is an enum or interface)
        Severity fakeSeverity = org.mockito.Mockito.mock(Severity.class);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convertToDatabaseColumn(fakeSeverity)
        );

        assertTrue(ex.getMessage().contains("Unknown Severity"));
    }
}
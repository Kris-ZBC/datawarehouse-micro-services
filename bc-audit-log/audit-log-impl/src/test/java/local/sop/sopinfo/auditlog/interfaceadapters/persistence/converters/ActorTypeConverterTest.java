package local.sop.sopinfo.auditlog.interfaceadapters.persistence.converters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.enums.ActorType;

class ActorTypeConverterTest {

    private final ActorTypeConverter converter = new ActorTypeConverter();

    @Test
    void convertToDatabaseColumn_shouldReturnNull_whenAttributeIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_shouldConvertEnumToLowercase() {
        assertEquals("user", converter.convertToDatabaseColumn(ActorType.USER));
        assertEquals("system", converter.convertToDatabaseColumn(ActorType.SYSTEM));
    }

    @Test
    void convertToEntityAttribute_shouldReturnNull_whenDbDataIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_shouldConvertLowercaseToEnum() {
        assertEquals(ActorType.USER, converter.convertToEntityAttribute("user"));
        assertEquals(ActorType.SYSTEM, converter.convertToEntityAttribute("system"));
    }

    @Test
    void convertToEntityAttribute_shouldConvertUppercaseToEnum() {
        assertEquals(ActorType.USER, converter.convertToEntityAttribute("USER"));
        assertEquals(ActorType.SYSTEM, converter.convertToEntityAttribute("SYSTEM"));
    }
}
package local.sop.sopinfo.sharedkernel.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class PhoneUserTypeTest {
    @Test
    void testPhoneUserTypeEnumComplete() {
        // Test alle values
        PhoneUserType[] types = PhoneUserType.values();
        assertEquals(5, types.length);
        
        // Test at alle expected values findes
        assertTrue(Arrays.asList(types).contains(PhoneUserType.SELF));
        assertTrue(Arrays.asList(types).contains(PhoneUserType.PARENT));
        assertTrue(Arrays.asList(types).contains(PhoneUserType.GUARDIAN));
        assertTrue(Arrays.asList(types).contains(PhoneUserType.EMERGENCY_CONTACT));
        assertTrue(Arrays.asList(types).contains(PhoneUserType.OTHER));
        
        // Test valueOf for alle
        assertEquals(PhoneUserType.SELF, PhoneUserType.valueOf("SELF"));
        assertEquals(PhoneUserType.PARENT, PhoneUserType.valueOf("PARENT"));
        assertEquals(PhoneUserType.GUARDIAN, PhoneUserType.valueOf("GUARDIAN"));
        assertEquals(PhoneUserType.EMERGENCY_CONTACT, PhoneUserType.valueOf("EMERGENCY_CONTACT"));
        assertEquals(PhoneUserType.OTHER, PhoneUserType.valueOf("OTHER"));
        
        // Test valueOf med invalid input
        assertThrows(IllegalArgumentException.class, () -> PhoneUserType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> PhoneUserType.valueOf("self")); // case-sensitive
        assertThrows(IllegalArgumentException.class, () -> PhoneUserType.valueOf("emergency_contact")); // underscore vs camelcase
        
        // Test toString for alle
        assertEquals("SELF", PhoneUserType.SELF.toString());
        assertEquals("PARENT", PhoneUserType.PARENT.toString());
        assertEquals("GUARDIAN", PhoneUserType.GUARDIAN.toString());
        assertEquals("EMERGENCY_CONTACT", PhoneUserType.EMERGENCY_CONTACT.toString());
        assertEquals("OTHER", PhoneUserType.OTHER.toString());
        
        // Test equals og hashCode
        assertEquals(PhoneUserType.SELF, PhoneUserType.SELF);
        assertNotEquals(PhoneUserType.SELF, PhoneUserType.PARENT);
        assertEquals(PhoneUserType.SELF.hashCode(), PhoneUserType.SELF.hashCode());
        
        // Test ordinal
        assertEquals(0, PhoneUserType.SELF.ordinal());
        assertEquals(1, PhoneUserType.PARENT.ordinal());
        assertEquals(2, PhoneUserType.GUARDIAN.ordinal());
        assertEquals(3, PhoneUserType.EMERGENCY_CONTACT.ordinal());
        assertEquals(4, PhoneUserType.OTHER.ordinal());
    }
}

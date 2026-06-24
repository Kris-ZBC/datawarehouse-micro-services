package local.sop.sopinfo.sharedkernel.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class SeverityTest {
    @Test
    void testSeverityEnumComplete() {
        // Test alle values
        Severity[] severities = Severity.values();
        assertEquals(5, severities.length);
        
        // Test at alle expected values findes
        assertTrue(Arrays.asList(severities).contains(Severity.DEBUG));
        assertTrue(Arrays.asList(severities).contains(Severity.INFO));
        assertTrue(Arrays.asList(severities).contains(Severity.WARNING));
        assertTrue(Arrays.asList(severities).contains(Severity.ERROR));
        assertTrue(Arrays.asList(severities).contains(Severity.FATAL));
        
        // Test valueOf for alle
        assertEquals(Severity.DEBUG, Severity.valueOf("DEBUG"));
        assertEquals(Severity.INFO, Severity.valueOf("INFO"));
        assertEquals(Severity.WARNING, Severity.valueOf("WARNING"));
        assertEquals(Severity.ERROR, Severity.valueOf("ERROR"));
        assertEquals(Severity.FATAL, Severity.valueOf("FATAL"));
        
        // Test valueOf med invalid input
        assertThrows(IllegalArgumentException.class, () -> Severity.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> Severity.valueOf("debug")); // case-sensitive
        assertThrows(IllegalArgumentException.class, () -> Severity.valueOf("warn")); // forkert navn
        assertThrows(IllegalArgumentException.class, () -> Severity.valueOf(""));
        assertThrows(NullPointerException.class, () -> Severity.valueOf(null));
        
        // Test toString for alle
        assertEquals("DEBUG", Severity.DEBUG.toString());
        assertEquals("INFO", Severity.INFO.toString());
        assertEquals("WARNING", Severity.WARNING.toString());
        assertEquals("ERROR", Severity.ERROR.toString());
        assertEquals("FATAL", Severity.FATAL.toString());
        
        // Test equals og hashCode
        assertEquals(Severity.DEBUG, Severity.DEBUG);
        assertNotEquals(Severity.DEBUG, Severity.INFO);
        assertEquals(Severity.DEBUG.hashCode(), Severity.DEBUG.hashCode());
        assertNotEquals(Severity.DEBUG.hashCode(), Severity.INFO.hashCode());
        
        // Test ordinal (rækkefølge)
        assertEquals(0, Severity.DEBUG.ordinal());
        assertEquals(1, Severity.INFO.ordinal());
        assertEquals(2, Severity.WARNING.ordinal());
        assertEquals(3, Severity.ERROR.ordinal());
        assertEquals(4, Severity.FATAL.ordinal());
        
        // Test compareTo (enums implementerer Comparable)
        assertTrue(Severity.DEBUG.compareTo(Severity.INFO) < 0);
        assertTrue(Severity.INFO.compareTo(Severity.DEBUG) > 0);
        assertEquals(0, Severity.ERROR.compareTo(Severity.ERROR));
        assertTrue(Severity.WARNING.compareTo(Severity.FATAL) < 0);
        assertTrue(Severity.FATAL.compareTo(Severity.WARNING) > 0);
    }
}

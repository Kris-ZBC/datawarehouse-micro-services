package local.sop.sopinfo.sharedkernel.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ActorTypeTest {

    @Test
    void testActorTypeEnum() {
        // Test alle values
        ActorType[] types = ActorType.values();
        assertEquals(3, types.length);
        
        // Test valueOf for alle
        assertEquals(ActorType.USER, ActorType.valueOf("USER"));
        assertEquals(ActorType.SERVICE, ActorType.valueOf("SERVICE"));
        assertEquals(ActorType.SYSTEM, ActorType.valueOf("SYSTEM"));
        
        // Test valueOf med invalid input
        assertThrows(IllegalArgumentException.class, () -> ActorType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> ActorType.valueOf("user")); // case-sensitive
        
        // Test toString for alle
        assertEquals("USER", ActorType.USER.toString());
        assertEquals("SERVICE", ActorType.SERVICE.toString());
        assertEquals("SYSTEM", ActorType.SYSTEM.toString());
        
        // Test equals og hashCode
        assertEquals(ActorType.USER, ActorType.USER);
        assertNotEquals(ActorType.USER, ActorType.SERVICE);
        assertEquals(ActorType.USER.hashCode(), ActorType.USER.hashCode());
    }
}

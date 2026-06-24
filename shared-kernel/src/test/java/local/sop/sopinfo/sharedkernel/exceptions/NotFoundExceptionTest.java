package local.sop.sopinfo.sharedkernel.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;


public class NotFoundExceptionTest {
  @Test
  void keyWithArgs_keepsCodeKeyAndArgs() {
    var ex = new NotFoundException("entity.notfound", Map.of("id", 123L));

    assertEquals(ErrorCode.NOT_FOUND, ex.code());
    assertEquals("entity.notfound", ex.messageKey());
    assertEquals(123L, ex.args().get("id"));
  }

  @Test
  void nullArgs_becomesEmptyMap() {
    var ex = new NotFoundException("entity.notfound", null);
    assertTrue(ex.args().isEmpty());
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void argsAreDefensivelyCopied_orUnmodifiable() {
    var input = new HashMap<String, Object>();
    input.put("id", 7L);

    var ex = new NotFoundException("entity.notfound", input);

    // mutate original -> must not leak
    input.put("leak", "nope");
    assertFalse(ex.args().containsKey("leak"));

    // returned map should be unmodifiable (Map.copyOf)
    assertThrows(UnsupportedOperationException.class, () -> {
      
      Map m = ex.args();     // raw cast to attempt mutation despite wildcard type
      m.put("hack", true);
    });
  }

  @Test
  void factory_student_buildsExpectedException() {
    var ex = NotFoundException.student(42L);

    assertEquals(ErrorCode.NOT_FOUND, ex.code());
    assertEquals("student.notFound", ex.messageKey()); // matches your factory literal
    assertEquals(42L, ex.args().get("id"));
  }

  @Test
  void toString_containsMessageKey() {
    var ex = new NotFoundException("entity.notfound", Map.of("id", "X"));
    var s = ex.toString();
    assertNotNull(s);
    assertTrue(s.contains("entity.notfound"));
  }
}

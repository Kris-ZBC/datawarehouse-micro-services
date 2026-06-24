package local.sop.sopinfo.sharedkernel.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

public class SubclassExceptionsTest {
    @Test
  void validationException_keepsKey() {
    var ex = new ValidationException("person.id.required", Map.of("field", "person-id"));
    assertTrue(ex instanceof DomainException);
    assertEquals("person.id.required", ex.messageKey());
  }

  @Test
  void conflictException_withArgs() {
    var ex = new ConflictException("person.email.exists", Map.of("field", "email"));
    assertEquals("person.email.exists", ex.messageKey());
    assertEquals("email", ex.args().get("field"));
  }

  @Test
  void notFoundException_basic() {
    var ex = new NotFoundException("person.not.found", Map.of("id", "123"));
    assertEquals("person.not.found", ex.messageKey());
    assertEquals("123", ex.args().get("id"));
  }

  @Test
  void concurrencyException_basic() {
    var ex = new ConcurrencyException("concurrency.conflict", Map.of("resource", "person"));
    assertEquals("concurrency.conflict", ex.messageKey());
    assertEquals("person", ex.args().get("resource"));
  }
}

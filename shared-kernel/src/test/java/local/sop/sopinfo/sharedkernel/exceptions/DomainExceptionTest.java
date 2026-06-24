 package local.sop.sopinfo.sharedkernel.exceptions;  

import org.junit.jupiter.api.Test;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {

  @Test
  void storesCodeMessageKeyAndArgs_andMessageEqualsMessageKey() {
    var anyCode = ErrorCode.values()[0]; // works regardless of enum constant names
    var args = Map.<String,Object>of(
        "field", "email",
        "value", "foo@bar"
    );
    var ex = new DomainException(anyCode, "person.email.exists", args);

    assertEquals(anyCode, ex.code());
    assertEquals("person.email.exists", ex.messageKey());
    assertEquals(args, ex.args());
    // superclass message is set to messageKey in your ctor
    assertEquals("person.email.exists", ex.getMessage());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  void argsAreDefensivelyCopied_orUnmodifiable() {
    var anyCode = ErrorCode.values()[0];
    var input = new java.util.HashMap<String,Object>();
    input.put("k", 1);

    var ex = new DomainException(anyCode, "x", input);

    // Mutating the original map must not affect the exception’s args
    input.put("leak", "nope");
    assertFalse(ex.args().containsKey("leak"));

    // Attempt to mutate the returned map: should throw UnsupportedOperationException
    assertThrows(UnsupportedOperationException.class, () -> {
      Map m = ex.args();     // cast to raw to bypass generics invariance
      m.put("hack", true);   // should throw
    });
  }

  @Test
  void toStringDoesNotThrow_andContainsMessageKey() {
    var anyCode = ErrorCode.values()[0];
    var ex = new DomainException(anyCode, "x", Map.of("a", 1));
    var s = ex.toString();
    assertNotNull(s);
    assertTrue(s.contains("x"));
  }

  @Test
  void ctor_withoutArgs_usesEmptyArgs() {
    var code = ErrorCode.values()[0];
    var ex = new DomainException(code, "x.key");
    assertEquals(code, ex.code());
    assertEquals("x.key", ex.messageKey());
    assertTrue(ex.args().isEmpty());
  }

    @Test
    void ctor_withCause_setsCause_andCopiesArgs() {
      var code = ErrorCode.values()[0];
      var cause = new IllegalStateException("boom");
      var ex = new DomainException(code, "x.key",
          java.util.Map.of("k", 1), cause);

      assertSame(cause, ex.getCause());
      assertEquals("x.key", ex.getMessage());
      assertEquals(1, ex.args().get("k"));
    }
}

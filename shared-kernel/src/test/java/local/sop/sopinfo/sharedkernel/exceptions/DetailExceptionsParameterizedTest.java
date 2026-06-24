package local.sop.sopinfo.sharedkernel.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


public class DetailExceptionsParameterizedTest {
    @ParameterizedTest(name = "{index} => {1} / {2}")
  @MethodSource("cases")
  void detailExceptions_haveExpectedCodeKeyAndArgs(
      Supplier<DomainException> factory,
      ErrorCode expectedCode,
      String expectedKey,
      Map<String, Object> expectedArgs
  ) {
    var ex = factory.get();

    assertEquals(expectedCode, ex.code(), "ErrorCode mismatch");
    assertEquals(expectedKey, ex.messageKey(), "messageKey mismatch");

    if (expectedArgs == null || expectedArgs.isEmpty()) {
      assertTrue(ex.args().isEmpty(), "args should be empty");
    } else {
      // exact match is fine
      assertEquals(expectedArgs, ex.args(), "args mismatch");
    }

    // toString should at least contain the key
    assertTrue(String.valueOf(ex).contains(expectedKey));
  }

  // --- Data set -------------------------------------------------------------

  static Stream<Arguments> cases() {
    return Stream.of(
        // NotFoundException (ctor)
        Arguments.of(
            (Supplier<DomainException>) () ->
                new NotFoundException("person.id.required", Map.of("field", "personid")),
            ErrorCode.NOT_FOUND,
            "person.id.required",
            Map.of("field", "personid")
        ),

        // NotFoundException factory
        Arguments.of(
            (Supplier<DomainException>) () -> NotFoundException.student(42L),
            ErrorCode.NOT_FOUND,
            "student.notFound",
            Map.of("id", 42L)
        ),

        // ValidationException (key-only)
        Arguments.of(
            (Supplier<DomainException>) () ->
                new ValidationException("person.id.required", Map.of("field", "personid")),
            ErrorCode.VALIDATION,
            "person.id.required",
            Map.of("field", "personid")
        ),

        // ConflictException (key + args)
        Arguments.of(
            (Supplier<DomainException>) () ->
                new ConflictException("person.id.required", Map.of("field", "personid")),
            ErrorCode.CONFLICT,
            "person.id.required",
            Map.of("field", "personid")
        ),
        // ConcurrencyException (key + args)
        Arguments.of(
            (Supplier<DomainException>) () ->
                new ConcurrencyException("person.id.required", Map.of("field", "personid")),
            ErrorCode.CONCURRENCY,
            "person.id.required",
            Map.of("field", "personid")
        ));
    
  }
}

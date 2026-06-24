package local.sop.sopinfo
.sharedkernel.valueobjects.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public class UUIDUtilTest {

    @Test
    void happypath_parseRequired_validUUID() {

        UUID expected = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try(MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
            mocked.when( () -> UUID.fromString(anyString()))
                .thenReturn(expected);
            UUID actual = UUIDUtil.parseRequired("123e4567-e89b-12d3-a456-426614174000", "id");
            assert(actual.equals(expected));
        }
    }

    @Test
    void unhappyPath_parseRequired_invalid_Should_throw_validation_exception() {
        
        String invalid = "invalid";
        try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
            mocked.when(() -> UUID.fromString(invalid))
                    .thenAnswer(inv -> {
                    var s = inv.getArgument(0, String.class);
                    throw new IllegalArgumentException("key.invalid: " + s);
                    });

            var ex = assertThrows(ValidationException.class,
                                    () -> UUIDUtil.parseRequired(invalid, "id"));

            assertEquals("key.invalid", ex.getMessage());

            // valgfrit: bekræft at stubben blev ramt med det rigtige argument
            mocked.verify(() -> UUID.fromString(invalid));
        }
  
        
    }

    @Test
    void unhappyPath_null_or_blank_Should_throw_validation_exception() {
        var ex1 = assertThrows(ValidationException.class,
                                () -> UUIDUtil.parseRequired(null, "id"));
        assertEquals("key.required", ex1.getMessage());

        var ex2 = assertThrows(ValidationException.class,
                                () -> UUIDUtil.parseRequired("   ", "id"));
        assertEquals("key.required", ex2.getMessage());
    }


    @Test
    void unhappyPath_parseRequired_invalidUUID() {
        
        try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
            mocked.when(() -> UUID.fromString(anyString()))
                    .thenAnswer(inv -> {
                    String s = inv.getArgument(0, String.class);
                    throw new ValidationException("key.invalid", Map.of(s, "id"));
                    });

            var ex = assertThrows(ValidationException.class,
                                    () -> UUIDUtil.parseRequired("invalid-uuid-string", "id"));

            assertEquals(ex.getMessage(), "key.invalid");

            // valgfrit: bekræft at stubben blev ramt med det rigtige argument
            mocked.verify(() -> UUID.fromString("invalid-uuid-string"));
        }
  
        
    }

    @Test
    void happypath_require_nonNullUUID() {
        
        var ex = assertThrows(ValidationException.class, () -> UUIDUtil.require(null, "id")) ;   
        assertEquals(ex.getMessage(), "key.required"); //UUID string cannot be null or blank
      
    }

    @Test
    void happyPath_require_validUUID() {
        UUID expected = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID actual = UUIDUtil.require(expected, "id");
        assertEquals(expected, actual);
    }   

    @Test
    void happyPath_newUuid_generatesUUID() {
        UUID generated = UUID.randomUUID();

        try (MockedStatic<UUID> mocked = mockStatic(UUID.class)) {
            mocked.when(UUID::randomUUID)
                    .thenReturn(generated);

            UUID actual = UUIDUtil.newUuid();
            assertEquals(generated, actual);

            // valgfrit: bekræft at stubben blev ramt
            mocked.verify(UUID::randomUUID);
        }
    }

}

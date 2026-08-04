package local.sop.sopinfo.login.domain.model.valueobjects;

import java.util.Map;
import java.util.regex.Pattern;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

/**
 * Represents a bcrypt-hashed password as stored in the database.
 *
 * Validates that the value conforms to the BCrypt hash format:
 *   $2a$, $2b$ or $2y$ — followed by cost factor (04-31) — followed by 53 Base64 characters
 *
 * This prevents plain text passwords from accidentally being stored
 * as a hashed password, and ensures the DB value is always a valid hash.
 *
 * Never constructed from plain text directly.
 * Constructed from PasswordEncoder output or from the database.
 */


public record HashedPassword(String value) {

    // BCrypt format: $2a$10$<22 chars salt><31 chars hash>
    // Variants: $2a$, $2b$, $2y$
    // Cost factor: 04-31 (two digits)
    // Remaining: 53 Base64-encoded characters (./A-Za-z0-9)
    private static final Pattern BCRYPT_PATTERN = Pattern.compile(
        "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$"
    );

    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw new ValidationException("login.password.invalid",
                Map.of("field", "password"));
        }
        if (!BCRYPT_PATTERN.matcher(value).matches()) {
            throw new ValidationException("login.password.not.hashed",
                Map.of("field", "password"));
        }
    }

    public static HashedPassword of(String hash) {
        return new HashedPassword(hash);
    }

}

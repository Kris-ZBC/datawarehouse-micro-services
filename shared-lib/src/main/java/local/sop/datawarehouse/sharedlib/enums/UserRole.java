package local.sop.datawarehouse.sharedlib.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public enum UserRole {
    TECHUSER,
    INSTRUCTOR,
    APPRENTICE;

    // NEW: mirrors LoginStatus.parse() exactly — same reasoning, same
    // shape. Wire-level DTOs carry role as a plain String (see
    // ApplicationLayerCleanTest's rule: DTO records may only depend on
    // JDK/Jakarta Validation/same-package types, nothing from
    // shared-lib); the actual enum conversion happens once, at the
    // application-service boundary, not at the DTO itself.
    public static UserRole parse(String role) throws ValidationException {
        Logger log = LoggerFactory.getLogger(UserRole.class);

        if (role == null) {
            log.warn("role is null!");
            throw new ValidationException("login.role.invalid", Map.of("field", "role"));
        }

        try {
            return valueOf(role);
        }
        catch (IllegalArgumentException ex) {
            log.warn("role is invalid {}", role);
            throw new ValidationException("login.role.invalid", Map.of("field", "role"));
        }
    }
}
package local.sop.common.libs.sharedkernel.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public enum ConsentType {
    REQUIRED,
    OPTIONAL,
    ONE_TIME;

    public static ConsentType parse(String type) throws ValidationException {
        Logger log = LoggerFactory.getLogger(ConsentType.class);
        
        if(type == null) {
            log.warn("type is null!");
            throw new ValidationException("consent.type.invalid", Map.of("field", "type"));
        }
        
        try {
            return valueOf(type.toUpperCase());
        }
        catch(IllegalArgumentException ex) {
            log.warn("type is invalid: {}", type);
            throw new ValidationException("consent.type.invalid", Map.of("field", "type"));
        }
    }
}

package local.sop.common.libs.sharedkernel.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public enum ConsentPurpose {
    MARKETING,
    ANALYTICS,
    PERSONALIZATION,
    THIRD_PARTY_SHARING,
    REQUIRED_SERVICE,
    COMMUNICATION,
    RESEARCH;

    public static ConsentPurpose parse(String purpose) throws ValidationException {
        Logger log = LoggerFactory.getLogger(ConsentPurpose.class);
        
        if(purpose == null) {
            log.warn("purpose is null!");
            throw new ValidationException("consent.purpose.invalid", Map.of("field", "purpose"));
        }
        
        try {
            return valueOf(purpose.toUpperCase());
        }
        catch(IllegalArgumentException ex) {
            log.warn("purpose is invalid: {}", purpose);
            throw new ValidationException("consent.purpose.invalid", Map.of("field", "purpose"));
        }
    }
}

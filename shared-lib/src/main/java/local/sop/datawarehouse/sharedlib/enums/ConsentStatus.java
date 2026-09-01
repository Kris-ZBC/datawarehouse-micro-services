package local.sop.datawarehouse.sharedlib.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public enum ConsentStatus {
    ACTIVE,
    WITHDRAWN,
    EXPIRED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canBeWithdrawn() {
        return this == ACTIVE;
    }

    public static ConsentStatus parse(String status) throws ValidationException {
        Logger log = LoggerFactory.getLogger(ConsentStatus.class);
        
        if(status == null) {
            log.warn("status is null!");
            throw new ValidationException("consent.status.invalid", Map.of("field", "status"));
        }
        
        try {
            return valueOf(status.toUpperCase());
        }
        catch(IllegalArgumentException ex) {
            log.warn("status is invalid: {}", status);
            throw new ValidationException("consent.status.invalid", Map.of("field", "status"));
        }
    }
}

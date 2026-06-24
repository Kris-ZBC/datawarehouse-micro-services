package local.sop.sopinfo.sharedkernel.exceptions;

import java.util.Map;

/**
 * Exception to be thrown when a conflict occurs, e.g., when trying to create a resource that already exists.
 */ 

public class ConflictException extends DomainException {
    public ConflictException(String messageKey, Map<String,Object> args) {
        super(ErrorCode.CONFLICT, messageKey, args);
    }
}

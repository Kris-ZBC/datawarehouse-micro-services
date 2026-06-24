package local.sop.sopinfo.sharedkernel.exceptions;

import java.util.Map;

public class ValidationException extends DomainException {
    public ValidationException(String messageKey, Map<String,Object> args) {
        super(ErrorCode.VALIDATION, messageKey, args);
    }
}

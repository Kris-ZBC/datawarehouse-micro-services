package local.sop.sopinfo.sharedkernel.exceptions;

import java.util.Map;

public class PreconditionException extends DomainException {
    public PreconditionException(String messageKey, Map<String,Object> args) {
        super(ErrorCode.PRECONDITION, messageKey, args);
    }
}

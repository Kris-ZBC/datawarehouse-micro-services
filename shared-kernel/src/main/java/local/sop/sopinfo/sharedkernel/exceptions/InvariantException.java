package local.sop.sopinfo.sharedkernel.exceptions;

import java.util.Map;

public class InvariantException extends DomainException {
    public InvariantException(String messageKey, Map<String,Object> args) {
        super(ErrorCode.INVARIANT, messageKey, args);
    }
}

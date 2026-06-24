package local.sop.sopinfo.sharedkernel.exceptions;

import java.util.Map;
import java.util.Objects;

/**
 * Pure domain exceptions 
 */
public class DomainException extends RuntimeException {
    private final ErrorCode code;
    private final String messageKey;
    private final Map<String, ?> args;

    public DomainException(ErrorCode code, String messageKey) {
        this(code, messageKey, Map.of(), null);
    }

    public DomainException(ErrorCode code, String messageKey, Map<String,Object> args) {
        this(code, messageKey, args, null);
    }

    public DomainException(ErrorCode code, String messageKey, Map<String,Object> args, Throwable cause) {
        super(Objects.requireNonNull(messageKey), cause);
        this.code = Objects.requireNonNull(code);
        this.messageKey = messageKey;
        this.args = args == null ? Map.of() : Map.copyOf(args);
    }

    public ErrorCode code() { return code; }
    public String messageKey() { return messageKey; }
    public Map<String,?> args() { return args; }
}


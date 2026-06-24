package local.sop.sopinfo.sharedkernel.sagas.compensate.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public enum SagaOutcome {
    SUCCEEDED, IDEMPOTENT, COMPENSATE, COMPENSATED, PARTIAL_FAILURE; 

    public static SagaOutcome parse(String outcome) throws ValidationException {
        Logger log = LoggerFactory.getLogger(SagaOutcome.class);
        if(outcome == null) {
            log.warn("outcome is null!");
            throw new ValidationException("saga.outcome.invalid", Map.of("field", "outcome"));
        }
        try {
            return valueOf(outcome);
        }
        catch(IllegalArgumentException ex) {
            log.warn("outcome is invalid {}", outcome);
            throw new ValidationException("saga.outcome.invalid", Map.of("field", "outcome"));
        }
    }
}

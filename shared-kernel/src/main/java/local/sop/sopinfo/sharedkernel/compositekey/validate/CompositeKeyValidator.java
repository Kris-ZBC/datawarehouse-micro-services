package local.sop.sopinfo.sharedkernel.compositekey.validate;

import java.util.UUID;

public interface CompositeKeyValidator {
    boolean exists(UUID id);
}

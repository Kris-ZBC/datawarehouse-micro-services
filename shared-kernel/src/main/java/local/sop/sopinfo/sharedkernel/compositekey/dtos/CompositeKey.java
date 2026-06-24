package local.sop.sopinfo.sharedkernel.compositekey.dtos;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public record CompositeKey(@NotNull UUID... keys) {
    public CompositeKey {
        if (keys == null || keys.length < 2) {
            throw new ValidationException("key.minimum",
                Map.of("minimum", "2", "actual", String.valueOf(keys == null ? 0 : keys.length)));
        }

    }
    public UUID key(int index) {
        return keys[index];
    }

    // convenience accessors for the common two-key case
    public UUID key1() { return keys[0]; }
    public UUID key2() { return keys[1]; }

}

package local.sop.sopinfo.workhour.domain.model.valueobjects;

/**
 * Invariant: SopRef must be a valid SOP key. This cannot be enforced from the value object, 
 * since it has no access to other microservices. Hence it must rely on the infrastructure to ensure that
 */

import java.util.UUID;

import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;
import local.sop.sopinfo.sharedkernel.valueobjects.utils.UUIDUtil;

public record SopRef(UUID value) implements DomainId {
    public SopRef {
        UUIDUtil.require(value, "sopRef");
    }

    public static SopRef of(UUID uuid) {
        return new SopRef(uuid);
    }

}

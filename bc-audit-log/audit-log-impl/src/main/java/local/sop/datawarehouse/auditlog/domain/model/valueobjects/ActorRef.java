package local.sop.datawarehouse.auditlog.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;
import local.sop.common.libs.sharedkernel.valueobjects.utils.UUIDUtil;

/**
 * Reference to the actor responsible for the event.
 */
public record ActorRef(UUID value) implements DomainId {

    public ActorRef {
        if (value == null ) {
            throw new ValidationException("log.actorRef.required", Map.of("field", "actorRef"));
        }
    }

    public static ActorRef newActorRef() {
        return new ActorRef(UUIDUtil.newUuid());
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}

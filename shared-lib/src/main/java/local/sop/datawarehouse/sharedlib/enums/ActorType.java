package local.sop.datawarehouse.sharedlib.enums;

/**
 * Actor type for audit log events.
 * Mirrors the shared-kernel design; kept locally until shared version exists.
 */
public enum ActorType {
    USER,
    SERVICE,
    SYSTEM
}

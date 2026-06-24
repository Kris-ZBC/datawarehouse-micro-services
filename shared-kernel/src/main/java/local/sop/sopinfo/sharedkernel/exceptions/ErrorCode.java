package local.sop.sopinfo.sharedkernel.exceptions;

public enum ErrorCode {
    VALIDATION,       // illegal input (domain rules)
    INVARIANT,        // Break of unbreakable
    NOT_FOUND,        // Entity not found
    CONFLICT,         // Conflict unique keys
    CONCURRENCY,      // optimistic locking
    PRECONDITION      // Needs state before handling
}

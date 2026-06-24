package local.sop.sopinfo.registration.saga.application.api.dto.apprentice;

import java.util.UUID;

public record ApprenticeResponse (
    UUID apprenticeId, 
    UUID personRef,
    UUID educationLineRef
) {}

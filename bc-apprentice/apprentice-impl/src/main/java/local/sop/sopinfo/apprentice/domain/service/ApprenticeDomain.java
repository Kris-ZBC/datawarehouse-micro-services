package local.sop.sopinfo.apprentice.domain.service;

import local.sop.sopinfo.apprentice.domain.model.Apprentice;
import local.sop.sopinfo.apprentice.domain.model.valueobjects.*;

public interface ApprenticeDomain {
    Apprentice create(ApprenticeId id, PersonRef personRef, EducationLineRef educationLineRef);
}

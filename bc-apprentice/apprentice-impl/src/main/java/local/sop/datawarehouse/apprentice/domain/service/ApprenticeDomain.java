package local.sop.datawarehouse.apprentice.domain.service;

import local.sop.datawarehouse.apprentice.domain.model.Apprentice;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.*;

public interface ApprenticeDomain {
    Apprentice create(ApprenticeId id, PersonRef personRef, EducationLineRef educationLineRef);
}

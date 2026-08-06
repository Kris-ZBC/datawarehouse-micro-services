package local.sop.datawarehouse.apprentice.domain.service;

import local.sop.datawarehouse.apprentice.domain.model.Apprentice;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.ApprenticeId;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.EducationLineRef;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.PersonRef;

public class ApprenticeDomainService implements ApprenticeDomain{

    @Override
    public Apprentice create(ApprenticeId id, PersonRef personRef, EducationLineRef educationLineRef) throws RuntimeException {
        /**
         * Any edge cases to handle?
         */
        return Apprentice.builder()
            .id(id)
            .personRef(personRef)
            .educationLineRef(educationLineRef)
            .build();
    }

}

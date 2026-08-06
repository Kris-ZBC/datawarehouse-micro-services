package local.sop.datawarehouse.apprentice.interfaceadapters.persistence.jpa;

import local.sop.datawarehouse.apprentice.domain.model.Apprentice;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.ApprenticeId;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.EducationLineRef;
import local.sop.datawarehouse.apprentice.domain.model.valueobjects.PersonRef;

public class ApprenticeJpaMapper {
    private ApprenticeJpaMapper() {}

    public static Apprentice toDomain(ApprenticeEntity entity) {
        return Apprentice.builder()
                .id(new ApprenticeId(entity.getId()))
                .personRef(new PersonRef(entity.getPersonRef()))
                .educationLineRef(new EducationLineRef(entity.getEducationLineRef()))
                .build();
    }

    public static ApprenticeEntity toEntity(Apprentice apprentice) {
        return new ApprenticeEntity(
                apprentice.getApprenticeId().value(),
                apprentice.getPersonRef().value(),
                apprentice.getEducationLineRef().value()
        );
    }
}

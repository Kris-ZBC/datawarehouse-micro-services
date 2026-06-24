package local.sop.sopinfo.apprentice.interfaceadapters.persistence.jpa;

import local.sop.sopinfo.apprentice.domain.model.Apprentice;
import local.sop.sopinfo.apprentice.domain.model.valueobjects.ApprenticeId;
import local.sop.sopinfo.apprentice.domain.model.valueobjects.EducationLineRef;
import local.sop.sopinfo.apprentice.domain.model.valueobjects.PersonRef;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApprenticeJpaMapperTest {

    private final UUID id = UUID.randomUUID();
    private final UUID personUuid = UUID.randomUUID();
    private final UUID eduUuid = UUID.randomUUID();

    @Test
    @DisplayName("Should map Entity to Domain correctly")
    void toDomain_ShouldMapAllFields() {
        // Arrange
        ApprenticeEntity entity = new ApprenticeEntity(id, personUuid, eduUuid);

        // Act
        Apprentice domain = ApprenticeJpaMapper.toDomain(entity);

        // Assert
        assertThat(domain.getApprenticeId().value()).isEqualTo(id);
        assertThat(domain.getPersonRef().value()).isEqualTo(personUuid);
        assertThat(domain.getEducationLineRef().value()).isEqualTo(eduUuid);
    }

    @Test
    @DisplayName("Should map Domain to Entity correctly")
    void toEntity_ShouldMapAllFields() {
        // Arrange
        Apprentice domain = Apprentice.builder()
                .id(new ApprenticeId(id))
                .personRef(new PersonRef(personUuid))
                .educationLineRef(new EducationLineRef(eduUuid))
                .build();

        // Act
        ApprenticeEntity entity = ApprenticeJpaMapper.toEntity(domain);

        // Assert
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getPersonRef()).isEqualTo(personUuid);
        assertThat(entity.getEducationLineRef()).isEqualTo(eduUuid);
    }

    @Test
    @DisplayName("Should cover private constructor for 100% coverage")
    void privateConstructor_ShouldBeCovered() throws Exception {
        // This is a common pattern to satisfy strict 100% coverage requirements 
        // for utility classes with private constructors.
        Constructor<ApprenticeJpaMapper> constructor = ApprenticeJpaMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ApprenticeJpaMapper instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}
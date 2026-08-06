package local.sop.datawarehouse.education.interfaceadapters.persistence.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.education.domain.model.Education;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationCategory;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationId;
import local.sop.datawarehouse.education.domain.model.valueobjects.EducationName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EducationJpaMapperTest {

    private EducationJpaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EducationJpaMapper();
    }

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        EducationEntity entity = new EducationEntity(id, "BSc Computer Science", "Engineering", true);

        Education domain = mapper.toDomain(entity);

        assertThat(domain.getId().value()).isEqualTo(id);
        assertThat(domain.getName().value()).isEqualTo("BSc Computer Science");
        assertThat(domain.getCategory().value()).isEqualTo("Engineering");
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    void shouldMapDomainToEntity() {
        EducationId id = EducationId.newId();
        Education domain = Education.builder()
                .id(id)
                .name(new EducationName("MSc Data Science"))
                .category(new EducationCategory("Science"))
                .active(false)
                .build();

        EducationEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id.value());
        assertThat(entity.getName()).isEqualTo("MSc Data Science");
        assertThat(entity.getCategory()).isEqualTo("Science");
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    void shouldMapEntityListToDomainList() {
        EducationEntity entity1 = new EducationEntity(UUID.randomUUID(), "Name 1", "Cat 1", true);
        EducationEntity entity2 = new EducationEntity(UUID.randomUUID(), "Name 2", "Cat 2", false);
        List<EducationEntity> entities = List.of(entity1, entity2);

        List<Education> result = mapper.toDomainList(entities);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName().value()).isEqualTo("Name 1");
        assertThat(result.get(1).getName().value()).isEqualTo("Name 2");
    }
}
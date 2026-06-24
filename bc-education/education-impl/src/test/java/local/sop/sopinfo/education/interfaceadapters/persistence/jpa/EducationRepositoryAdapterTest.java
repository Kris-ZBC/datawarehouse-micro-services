package local.sop.sopinfo.education.interfaceadapters.persistence.jpa;

import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationCategory;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationId;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationRepositoryAdapterTest {

    @Mock
    private EducationSpringDataRepository jpaRepository;

    private EducationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EducationRepositoryAdapter(jpaRepository);
    }

    @Test
    void shouldCreateEducation() {
        EducationId id = EducationId.newId();
        Education education = Education.builder()
                .id(id)
                .name(new EducationName("IT Support"))
                .category(new EducationCategory("Data and Communication"))
                .active(false)
                .build();

        EducationEntity entity = new EducationEntity();
        entity.setId(id.value());
        entity.setName("IT Support");
        entity.setCategory("Data and Communication");
        entity.setActive(false);

        when(jpaRepository.save(any(EducationEntity.class))).thenReturn(entity);

        Education result = adapter.create(education);

        assertThat(result).isNotNull();
        assertThat(result.getName().value()).isEqualTo("IT Support");
        assertThat(result.getCategory().value()).isEqualTo("Data and Communication");
        verify(jpaRepository).save(any(EducationEntity.class));
    }

    @Test
    void shouldFindById() {
        UUID uuid = UUID.randomUUID();
        EducationId id = EducationId.parse(uuid.toString());
        EducationEntity entity = new EducationEntity();
        entity.setId(uuid);
        entity.setName("Found");
        entity.setCategory("Test");

        when(jpaRepository.findById(uuid)).thenReturn(Optional.of(entity));

        Optional<Education> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId().value()).isEqualTo(uuid);
        verify(jpaRepository).findById(uuid);
    }

    @Test
void shouldActivateEducation() {
    EducationId id = EducationId.newId();
    Education education = Education.builder()
            .id(id)
            .name(new EducationName("IT Support")) // Added
            .category(new EducationCategory("Data and Communication")) // Added
            .build();

    adapter.activate(education);

    verify(jpaRepository).activate(id.value());
}

    @Test
    void shouldDeactivateEducation() {
        EducationId id = EducationId.newId();
        Education education = Education.builder()
            .id(id)
            .name(new EducationName("IT Support")) // Added
            .category(new EducationCategory("Data and Communication")) // Added
            .build();

        adapter.deactivate(education);

        verify(jpaRepository).deactivate(id.value());
    }

    @Test
    void shouldUpdateName() {
        EducationId id = EducationId.newId();
        Education education = Education.builder()
                .id(id)
                .name(new EducationName("Updated Name"))
                .category(new EducationCategory("Updated Category"))
                .active(true)
                .build();

        EducationEntity entity = new EducationEntity();
        entity.setId(id.value());
        entity.setName("Updated Name");
        entity.setCategory("Updated Category");
        entity.setActive(true);

        when(jpaRepository.save(any(EducationEntity.class))).thenReturn(entity);

        Education result = adapter.updateName(education);

        assertThat(result.getName().value()).isEqualTo("Updated Name");
        verify(jpaRepository).save(any(EducationEntity.class));
    }

    @Test
    void shouldUpdateCategory() {
        EducationId id = EducationId.newId();
        Education education = Education.builder()
                .id(id)
                .name(new EducationName("Updated Name"))
                .category(new EducationCategory("Updated Category"))
                .active(true)
                .build();

        EducationEntity entity = new EducationEntity();
        entity.setId(id.value());
        entity.setName("Updated Name");
        entity.setCategory("Updated Category");
        entity.setActive(true);

        when(jpaRepository.save(any(EducationEntity.class))).thenReturn(entity);

        Education result = adapter.updateCategory(education);

        assertThat(result.getCategory().value()).isEqualTo("Updated Category");
        verify(jpaRepository).save(any(EducationEntity.class));
    }

    @Test
    void shouldFindAll() {
        EducationEntity entity1 = new EducationEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setName("IT Support");
        entity1.setCategory("Data and Communication");

        EducationEntity entity2 = new EducationEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setName("Programming");
        entity2.setCategory("Data and Communication");

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<Education> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName().value()).isEqualTo("IT Support");
        verify(jpaRepository).findAll();
    }
}
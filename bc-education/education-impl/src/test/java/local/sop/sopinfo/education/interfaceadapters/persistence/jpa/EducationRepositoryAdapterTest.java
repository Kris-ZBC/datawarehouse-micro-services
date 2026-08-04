package local.sop.sopinfo.education.interfaceadapters.persistence.jpa;

import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationCategory;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationId;
import local.sop.sopinfo.education.domain.model.valueobjects.EducationName;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Nested
    class compensateCreate{
        
        //HappyPath
        @Test
        void shouldCompensateCreate() {
            EducationId id = EducationId.newId();

            EducationEntity entity = new EducationEntity();
            entity.setId(id.value());
            entity.setName("IT Support");
            entity.setCategory("Data and Communication");

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.of(entity));

            Boolean result = adapter.compensateCreate(id, SagaOutcome.COMPENSATE);

            assertThat(result).isTrue();
            verify(jpaRepository).deleteById(id.value());
        }

        // unHappyPath
        @Test
        void shouldThrowConflictExceptionWhenCompensateCreateCalledWithWrongSagaState() {
            EducationId id = EducationId.newId();

            assertThatThrownBy(() -> adapter.compensateCreate(id, SagaOutcome.COMPENSATED))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("compensate.wrong.state");
        }

        //branch coverage
        @Test
        void shouldCompensateCreateWhenEducationDoesNotExist() {
            EducationId id = EducationId.newId();

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.empty());

            Boolean result = adapter.compensateCreate(id, SagaOutcome.COMPENSATE);

            assertThat(result).isTrue();

            verify(jpaRepository).findById(id.value());
            verify(jpaRepository).deleteById(id.value());
        }
    }
    @Nested
    class compensateActivate {
        @Test
        void compensateActivate_shouldThrowConflictException_whenSagaStateIsNotCompensate() {
            EducationId id = EducationId.newId();

            assertThrows(ConflictException.class,
                    () -> adapter.compensateActivate(id, SagaOutcome.COMPENSATED));

            verify(jpaRepository, never()).updateActive(any(), any(Boolean.class));
        }

        @Test
        void compensateActivate_shouldUpdateActiveToFalse_whenEducationExists() {
            EducationId id = EducationId.newId();

            EducationEntity entity = new EducationEntity();
            entity.setId(id.value());
            entity.setName("IT Support");
            entity.setCategory("Data and Communication");

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.of(entity));

            Boolean result = adapter.compensateActivate(id, SagaOutcome.COMPENSATE);

            assertThat(result).isTrue();
            verify(jpaRepository).updateActive(id.value(), false);
        }

        @Test
        void compensateActivate_shouldStillUpdateActiveToFalse_whenEducationDoesNotExist() {
            EducationId id = EducationId.newId();

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.empty());

            Boolean result = adapter.compensateActivate(id, SagaOutcome.COMPENSATE);

            assertThat(result).isTrue();
            verify(jpaRepository).updateActive(id.value(), false);
        }
    }

    @Nested
    class compensateDeactivate {

        @Test
        void compensateDeactivate_shouldThrowConflictException_whenSagaStateIsNotCompensate() {
            EducationId id = EducationId.newId();

            assertThrows(ConflictException.class,
                    () -> adapter.compensateDeactivate(id, SagaOutcome.COMPENSATED));

            verify(jpaRepository, never()).updateActive(any(), any(Boolean.class));
        }

        @Test
        void compensateDeactivate_shouldUpdateActiveToTrue_whenEducationExists() {
            EducationId id = EducationId.newId();

            EducationEntity entity = new EducationEntity();
            entity.setId(id.value());
            entity.setName("IT Support");
            entity.setCategory("Data and Communication");

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.of(entity));

            Boolean result = adapter.compensateDeactivate(id, SagaOutcome.COMPENSATE);

            assertThat(result).isTrue();
            verify(jpaRepository).updateActive(id.value(), true);
        }

        @Test
        void compensateDeactivate_shouldStillUpdateActiveToTrue_whenEducationDoesNotExist() {
            EducationId id = EducationId.newId();

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.empty());

            Boolean result = adapter.compensateDeactivate(id, SagaOutcome.COMPENSATE);

            assertThat(result).isTrue();
            verify(jpaRepository).updateActive(id.value(), true);
        }
    }

    @Nested
    class compensateUpdateName {
        @Test
        void compensateUpdateName_shouldThrowConflictException_whenSagaStateIsNotCompensate() {
            EducationId id = EducationId.newId();

            assertThrows(ConflictException.class,
                    () -> adapter.compensateUpdateName(id, SagaOutcome.COMPENSATED, "Old Name"));

            verify(jpaRepository, never()).updateName(any(), any());
        }

        @Test
        void compensateUpdateName_shouldRestoreOldName_whenEducationExists() {
            EducationId id = EducationId.newId();
            String oldName = "Old Name";

            EducationEntity entity = new EducationEntity();
            entity.setId(id.value());
            entity.setName("Current Name");
            entity.setCategory("Data and Communication");

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.of(entity));

            Boolean result = adapter.compensateUpdateName(id, SagaOutcome.COMPENSATE, oldName);

            assertThat(result).isTrue();
            verify(jpaRepository).updateName(id.value(), oldName);
        }

        @Test
        void compensateUpdateName_shouldStillRestoreOldName_whenEducationDoesNotExist() {
            EducationId id = EducationId.newId();
            String oldName = "Old Name";

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.empty());

            Boolean result = adapter.compensateUpdateName(id, SagaOutcome.COMPENSATE, oldName);

            assertThat(result).isTrue();
            verify(jpaRepository).updateName(id.value(), oldName);
        }
    }

    @Nested
    class compensateUpdateCategory {
        @Test
        void compensateUpdateCategory_shouldThrowConflictException_whenSagaStateIsNotCompensate() {
            EducationId id = EducationId.newId();

            assertThrows(ConflictException.class,
                    () -> adapter.compensateUpdateCategory(
                            id,
                            SagaOutcome.COMPENSATED,
                            "Old Category"));

            verify(jpaRepository, never()).updateCategory(any(), any());
        }

        @Test
        void compensateUpdateCategory_shouldRestoreOldCategory_whenEducationExists() {
            EducationId id = EducationId.newId();
            String oldCategory = "Old Category";

            EducationEntity entity = new EducationEntity();
            entity.setId(id.value());
            entity.setName("IT Support");
            entity.setCategory("Current Category");

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.of(entity));

            Boolean result = adapter.compensateUpdateCategory(
                    id,
                    SagaOutcome.COMPENSATE,
                    oldCategory);

            assertThat(result).isTrue();
            verify(jpaRepository).updateCategory(id.value(), oldCategory);
        }

        @Test
        void compensateUpdateCategory_shouldStillRestoreOldCategory_whenEducationDoesNotExist() {
            EducationId id = EducationId.newId();
            String oldCategory = "Old Category";

            when(jpaRepository.findById(id.value()))
                    .thenReturn(Optional.empty());

            Boolean result = adapter.compensateUpdateCategory(
                    id,
                    SagaOutcome.COMPENSATE,
                    oldCategory);

            assertThat(result).isTrue();
            verify(jpaRepository).updateCategory(id.value(), oldCategory);
        }
    }
}
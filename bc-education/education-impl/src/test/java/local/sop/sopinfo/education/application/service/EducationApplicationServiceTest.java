package local.sop.sopinfo.education.application.service;

import local.sop.sopinfo.education.application.api.dto.*;
import local.sop.sopinfo.education.domain.model.Education;
import local.sop.sopinfo.education.domain.model.valueobjects.*;
import local.sop.sopinfo.education.domain.ports.out.EducationRepositoryPort;
import local.sop.common.libs.sharedkernel.exceptions.NotFoundException;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.common.libs.sharedkernel.sagas.compensate.response.ResponseCompensated;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EducationApplicationServiceTest {

    @Mock
    private EducationRepositoryPort repository;

    @InjectMocks
    private EducationApplicationService applicationService;

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────
    @Nested
    class CreateTests {

        @Test
        void shouldCreateEducation() {
            CreateEducationCmd cmd = new CreateEducationCmd("New Course", "Category");
            Education savedEducation = createValidEducation(UUID.randomUUID(), "New Course");

            when(repository.existsByName(any(EducationName.class))).thenReturn(false);
            when(repository.create(any(Education.class))).thenReturn(savedEducation);

            EducationResponse response = applicationService.createEducation(cmd);

            assertThat(response.name()).isEqualTo("New Course");
            verify(repository).create(any(Education.class));
        }

        @Test
        void shouldThrowExceptionWhenCreatingEducationWithExistingName() {
            CreateEducationCmd cmd = new CreateEducationCmd("Existing Course", "Category");

            when(repository.existsByName(any(EducationName.class))).thenReturn(true);

            assertThatThrownBy(() -> applicationService.createEducation(cmd))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("education.name.exists");

            verify(repository, never()).create(any());
        }
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────
    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateEducationName() {
            UUID id = UUID.randomUUID();
            String newName = "Updated Name";
            Education existing = createValidEducation(id, "Old Name");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.existsByName(any(EducationName.class))).thenReturn(false);
            when(repository.updateName(any(Education.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            EducationResponse response = applicationService.updateEducationName(id, newName);

            assertThat(response.name()).isEqualTo("Updated Name");
            verify(repository).updateName(any(Education.class));
        }

        @Test
        void shouldUpdateEducationCategory() {
            UUID id = UUID.randomUUID();
            String newCategory = "Updated Category";
            Education existing = createValidEducation(id, "Name");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.updateCategory(any(Education.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            EducationResponse response = applicationService.updateEducationCategory(id, newCategory);

            assertThat(response.category()).isEqualTo("Updated Category");
            verify(repository).updateCategory(any(Education.class));
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNameToExistingName() {
            UUID id = UUID.randomUUID();
            String takenName = "Taken Name";
            Education existing = createValidEducation(id, "Original Name");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.existsByName(any(EducationName.class))).thenReturn(true);

            assertThatThrownBy(() -> applicationService.updateEducationName(id, takenName))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("education.name.exists");
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNameForNonExistentEducation() {
            UUID id = UUID.randomUUID();
            String newName = "IT Support";

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateEducationName(id, newName))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("education.not.found");
        }

        @Test
        void shouldThrowNotFoundExceptionWhenUpdatingCategoryForNonExistingEducation() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.updateEducationCategory(id, "New Category"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("education.not.found");

            verify(repository).findById(any(EducationId.class));
            verify(repository, never()).updateCategory(any(Education.class));
        }

        @Test
        void shouldThrowValidationExceptionWhenUpdatingToExistingCategory() {
            UUID id = UUID.randomUUID();

            Education existing = Education.builder()
                    .id(EducationId.parse(id.toString()))
                    .name(new EducationName("Test"))
                    .category(new EducationCategory("Current Category"))
                    .active(true)
                    .build();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.of(existing));

            when(repository.existsByCategory(any(EducationCategory.class)))
                    .thenReturn(true);

            assertThatThrownBy(() -> applicationService.updateEducationCategory(id, "Taken Category"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("education.category.exists");

            verify(repository).findById(any(EducationId.class));
            verify(repository).existsByCategory(any(EducationCategory.class));
            verify(repository, never()).updateCategory(any(Education.class));
        }
    }

    // ─────────────────────────────────────────────
    // FIND
    // ─────────────────────────────────────────────
    @Nested
    class FindTests {

        @Test
        void shouldFindAllEducations() {
            Education edu1 = createValidEducation(UUID.randomUUID(), "IT Support");
            Education edu2 = createValidEducation(UUID.randomUUID(), "Programming");

            when(repository.findAll()).thenReturn(List.of(edu1, edu2));

            List<EducationResponse> responses = applicationService.findAll();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).name()).isEqualTo("IT Support");
            verify(repository).findAll();
        }

        @Test
        void shouldFindEducationById() {
            UUID id = UUID.randomUUID();
            Education existing = createValidEducation(id, "IT Support");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));

            Optional<EducationResponse> response = applicationService.findById(id);

            assertThat(response).isPresent();
            assertThat(response.get().name()).isEqualTo("IT Support");
        }

        @Test
        void shouldReturnEmptyWhenEducationNotFound() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.empty());

            Optional<EducationResponse> response = applicationService.findById(id);

            assertThat(response).isEmpty();
        }
    }

    // ─────────────────────────────────────────────
    // ACTIVATE / DEACTIVATE
    // ─────────────────────────────────────────────
    @Nested
    class ActivationTests {

        @Test
        void shouldActivateEducation() {
            UUID id = UUID.randomUUID();
            Education existing = createValidEducation(id, "Inactive Course");

            existing = Education.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .category(existing.getCategory())
                    .active(false)
                    .build();

            Education activatedEducation = Education.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .category(existing.getCategory())
                    .active(true)
                    .build();

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.activate(any(Education.class))).thenReturn(activatedEducation);

            EducationResponse response = applicationService.activateEducation(id);

            assertThat(response.isActive()).isTrue();
            verify(repository).activate(any(Education.class));
        }

        @Test
        void shouldDeactivateEducation() {
            UUID id = UUID.randomUUID();
            Education existing = createValidEducation(id, "Active Course");

            existing = Education.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .category(existing.getCategory())
                    .active(true)
                    .build();

            Education deactivatedEducation = Education.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .category(existing.getCategory())
                    .active(false)
                    .build();

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.deactivate(any(Education.class))).thenReturn(deactivatedEducation);

            EducationResponse response = applicationService.deactivateEducation(id);

            assertThat(response.isActive()).isFalse();
            verify(repository).deactivate(any(Education.class));
        }

        @Test
        void shouldThrowExceptionWhenActivatingNonExistentEducation() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.activateEducation(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("education.not.found");
        }

        @Test
        void shouldThrowNotFoundExceptionWhenDeactivatingNonExistingEducation() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.deactivateEducation(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("education.not.found");

            verify(repository).findById(any(EducationId.class));
            verify(repository, never()).deactivate(any(Education.class));
        }

        @Test
        void shouldThrowValidationExceptionWhenDeactivatingAlreadyInactiveEducation() {
            UUID id = UUID.randomUUID();

            Education inactiveEducation = Education.builder()
                    .id(EducationId.parse(id.toString()))
                    .name(new EducationName("Test"))
                    .category(new EducationCategory("Data and Communication"))
                    .active(false)
                    .build();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.of(inactiveEducation));

            assertThatThrownBy(() -> applicationService.deactivateEducation(id))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("education.already.inactive");

            verify(repository).findById(any(EducationId.class));
            verify(repository, never()).deactivate(any(Education.class));
        }

        @Test
        void shouldThrowValidationExceptionWhenActivatingAlreadyActiveEducation() {
            UUID id = UUID.randomUUID();

            Education activeEducation = Education.builder()
                    .id(EducationId.parse(id.toString()))
                    .name(new EducationName("Test"))
                    .category(new EducationCategory("Data and Communication"))
                    .active(true)
                    .build();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.of(activeEducation));

            assertThatThrownBy(() -> applicationService.activateEducation(id))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("education.already.active");

            verify(repository).findById(any(EducationId.class));
            verify(repository, never()).activate(any(Education.class));
        }
    }

    // ─────────────────────────────────────────────
    // COMPENSATION
    // ─────────────────────────────────────────────
    @Nested
    class CompensationTests {

        @Test
        void shouldCompensateCreateEducation() {
            UUID id = UUID.randomUUID();
            Education existing = createValidEducation(id, "Test Education");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.compensateCreate(any(EducationId.class), any(SagaOutcome.class))).thenReturn(true);

            ResponseCompensated response = applicationService.compensateCreateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(response).isNotNull();
            verify(repository).compensateCreate(any(EducationId.class), any(SagaOutcome.class));
        }

        @Test
        void shouldCompensateUpdateEducationName() {
            UUID id = UUID.randomUUID();
            String previousName = "Old Name";
            Education existing = createValidEducation(id, "Test Education");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.compensateUpdateName(any(EducationId.class), any(SagaOutcome.class), any(String.class)))
                    .thenReturn(true);

            ResponseCompensated response = applicationService.compensateUpdateEducationName(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATED,
                    previousName);

            assertThat(response).isNotNull();
            verify(repository).compensateUpdateName(any(EducationId.class), any(SagaOutcome.class), any(String.class));
        }

        @Test
        void shouldCompensateUpdateEducationCategory() {
            UUID id = UUID.randomUUID();
            String previousCategory = "Old Category";
            Education existing = createValidEducation(id, "Test Education");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.compensateUpdateCategory(any(EducationId.class), any(SagaOutcome.class), any(String.class)))
                    .thenReturn(true);

            ResponseCompensated response = applicationService.compensateUpdateEducationCategory(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATED,
                    previousCategory);

            assertThat(response).isNotNull();
            verify(repository).compensateUpdateCategory(any(EducationId.class), any(SagaOutcome.class),
                    any(String.class));
        }

        @Test
        void shouldCompensateActivateEducation() {
            UUID id = UUID.randomUUID();
            Education existing = createValidEducation(id, "Test Education");

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.compensateActivate(any(EducationId.class), any(SagaOutcome.class))).thenReturn(true);

            ResponseCompensated response = applicationService.compensateActivateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(response).isNotNull();
            verify(repository).compensateActivate(any(EducationId.class), any(SagaOutcome.class));
        }

        @Test
        void shouldCompensateDeactivateEducation() {
            UUID id = UUID.randomUUID();
            Education existing = createValidEducation(id, "Test Education");

            existing = Education.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .category(existing.getCategory())
                    .active(false)
                    .build();

            when(repository.findById(any(EducationId.class))).thenReturn(Optional.of(existing));
            when(repository.compensateDeactivate(any(EducationId.class), any(SagaOutcome.class))).thenReturn(true);

            ResponseCompensated response = applicationService.compensateDeactivateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(response).isNotNull();
            verify(repository).compensateDeactivate(any(EducationId.class), any(SagaOutcome.class));
        }

        @Test
        void shouldReturnIdempotentFalse_whenEducationNotFoundForCompensation() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.empty());

            ResponseCompensated result = applicationService.compensateUpdateEducationName(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATED,
                    "old-name");

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isFalse();

            verify(repository, never())
                    .compensateUpdateName(any(), any(), any());
        }

        @Test
        void shouldReturnIdempotentTrue_whenEducationNameAlreadyInPreviousState() {
            UUID id = UUID.randomUUID();
            String previousName = "Old Name";

            Education existing = createValidEducation(id, previousName);

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.of(existing));

            ResponseCompensated result = applicationService.compensateUpdateEducationName(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATED,
                    previousName);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isTrue();

            verify(repository, never())
                    .compensateUpdateName(any(), any(), any());
        }

        @Test
        void shouldReturnIdempotentFalse_whenEducationNotFoundDuringCompensation() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.empty());

            ResponseCompensated result = applicationService.compensateUpdateEducationCategory(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE,
                    "old-category");

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isFalse();

            verify(repository, never())
                    .compensateUpdateCategory(any(), any(), any());
        }

        @Test
        void shouldReturnIdempotentFalse_whenEducationNotFoundForActivateCompensation() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.empty());

            ResponseCompensated result = applicationService.compensateActivateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isFalse();

            verify(repository, never())
                    .compensateActivate(any(), any());
        }

        @Test
        void shouldReturnIdempotentTrue_whenEducationAlreadyActive() {
            UUID id = UUID.randomUUID();

            Education existing = Education.builder()
                    .id(EducationId.parse(id.toString()))
                    .name(new EducationName("Test"))
                    .category(new EducationCategory("Data"))
                    .active(true)
                    .build();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.of(existing));

            ResponseCompensated result = applicationService.compensateDeactivateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isTrue();

            verify(repository, never())
                    .compensateActivate(any(), any());
        }

        @Test
        void shouldReturnIdempotentFalse_whenEducationNotFoundForDeactivateCompensation() {
            UUID id = UUID.randomUUID();
            EducationId educationId = new EducationId(id);

            when(repository.findById(educationId))
                    .thenReturn(Optional.empty());

            ResponseCompensated result = applicationService.compensateDeactivateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isFalse();

            verify(repository).findById(educationId);

            verify(repository, never())
                    .compensateDeactivate(any(), any());
        }

        @Test
        void shouldReturnIdempotentTrue_whenEducationAlreadyInactiveDuringActivateCompensation() {
            UUID id = UUID.randomUUID();
            EducationId educationId = new EducationId(id);

            Education existing = Education.builder()
                    .id(educationId)
                    .name(new EducationName("Test"))
                    .category(new EducationCategory("Data"))
                    .active(false) // 👈 KEY: already inactive
                    .build();

            when(repository.findById(educationId))
                    .thenReturn(Optional.of(existing));

            ResponseCompensated result = applicationService.compensateActivateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isTrue();

            verify(repository).findById(educationId);

            verify(repository, never())
                    .compensateActivate(any(), any());
        }

        @Test
        void shouldReturnIdempotentTrue_whenEducationCategoryAlreadyInPreviousState() {
            UUID id = UUID.randomUUID();
            EducationId educationId = new EducationId(id);
            String previousCategory = "Data";

            Education existing = Education.builder()
                    .id(educationId)
                    .name(new EducationName("Test"))
                    .category(new EducationCategory(previousCategory))
                    .active(true)
                    .build();

            when(repository.findById(educationId))
                    .thenReturn(Optional.of(existing));

            ResponseCompensated result = applicationService.compensateUpdateEducationCategory(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE,
                    previousCategory);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isTrue();

            verify(repository).findById(educationId);

            verify(repository, never())
                    .compensateUpdateCategory(any(), any(), any());
        }

        @Test
        void shouldReturnIdempotentFalseWhenEducationNotFoundForCompensateCreate() {
            UUID id = UUID.randomUUID();

            when(repository.findById(any(EducationId.class)))
                    .thenReturn(Optional.empty());

            ResponseCompensated result = applicationService.compensateCreateEducation(
                    id,
                    EducationApplicationServiceTest.class,
                    SagaOutcome.COMPENSATE);

            assertThat(result).isNotNull();
            assertThat(result.sagaState()).isEqualTo(SagaOutcome.IDEMPOTENT);
            assertThat(result.success()).isFalse();

            verify(repository).findById(any(EducationId.class));
            verify(repository, never())
                    .compensateCreate(any(EducationId.class), any(SagaOutcome.class));
        }

    }

    // ─────────────────────────────────────────────
    // helper
    // ─────────────────────────────────────────────
    private Education createValidEducation(UUID id, String name) {
        return Education.builder()
                .id(EducationId.parse(id.toString()))
                .name(new EducationName(name))
                .category(new EducationCategory("Data and Communication"))
                .active(true)
                .build();
    }
}
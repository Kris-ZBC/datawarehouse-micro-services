package local.sop.sopinfo.apprentice.interfaceadapters.persistence.jpa;

import local.sop.sopinfo.apprentice.domain.model.Apprentice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprenticeRepositoryAdapterTest {

    @Mock
    private ApprenticeSpringDataRepository jpaRepository;

    private ApprenticeRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ApprenticeRepositoryAdapter(jpaRepository);
    }

    @Test
    @DisplayName("Should generate new UUID and save when entity ID is null")
    void save_WhenIdIsNull_ShouldGenerateUuidAndSave() {
        try (MockedStatic<ApprenticeJpaMapper> mapper = mockStatic(ApprenticeJpaMapper.class)) {
            // Arrange
            Apprentice domain = mock(Apprentice.class);
            ApprenticeEntity entityWithoutId = new ApprenticeEntity(); // ID is null
            ApprenticeEntity savedEntity = new ApprenticeEntity().withId(UUID.randomUUID());
            Apprentice savedDomain = mock(Apprentice.class);

            mapper.when(() -> ApprenticeJpaMapper.toEntity(domain)).thenReturn(entityWithoutId);
            when(jpaRepository.save(any(ApprenticeEntity.class))).thenReturn(savedEntity);
            mapper.when(() -> ApprenticeJpaMapper.toDomain(savedEntity)).thenReturn(savedDomain);

            // Act
            Apprentice result = adapter.save(domain);

            // Assert
            assertNotNull(result);
            verify(jpaRepository).save(argThat(entity -> entity.getId() != null));
        }
    }

    @Test
    @DisplayName("Should save without generating new UUID when ID already exists")
    void save_WhenIdExists_ShouldNotGenerateNewUuid() {
        try (MockedStatic<ApprenticeJpaMapper> mapper = mockStatic(ApprenticeJpaMapper.class)) {
            UUID existingId = UUID.randomUUID();
            Apprentice domain = mock(Apprentice.class);
            ApprenticeEntity entityWithId = new ApprenticeEntity().withId(existingId);
            Apprentice savedDomain = mock(Apprentice.class);

            mapper.when(() -> ApprenticeJpaMapper.toEntity(domain)).thenReturn(entityWithId);
            when(jpaRepository.save(entityWithId)).thenReturn(entityWithId);
            mapper.when(() -> ApprenticeJpaMapper.toDomain(entityWithId)).thenReturn(savedDomain);

            // Act
            adapter.save(domain);

            // Assert
            verify(jpaRepository).save(argThat(entity -> entity.getId().equals(existingId)));
        }
    }

    @Test
    @DisplayName("Should find apprentice by ID")
    void findById_ShouldReturnDomainObject() {
        try (MockedStatic<ApprenticeJpaMapper> mapper = mockStatic(ApprenticeJpaMapper.class)) {
            UUID id = UUID.randomUUID();
            ApprenticeEntity entity = new ApprenticeEntity();
            Apprentice domain = mock(Apprentice.class);

            when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
            mapper.when(() -> ApprenticeJpaMapper.toDomain(entity)).thenReturn(domain);

            Optional<Apprentice> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(domain, result.get());
        }
    }

    @Test
    @DisplayName("Should find apprentice by Education Line ID")
    void findByEducationLineId_ShouldReturnDomainObject() {
        try (MockedStatic<ApprenticeJpaMapper> mapper = mockStatic(ApprenticeJpaMapper.class)) {
            UUID eduId = UUID.randomUUID();
            ApprenticeEntity entity = new ApprenticeEntity();
            Apprentice domain = mock(Apprentice.class);

            when(jpaRepository.findByEducationLineRef(eduId)).thenReturn(List.of(entity));
            mapper.when(() -> ApprenticeJpaMapper.toDomain(entity)).thenReturn(domain);

            List<Apprentice> result = adapter.findByEducationLineId(eduId);

            assertThat(result).hasSize(1);
            verify(jpaRepository).findByEducationLineRef(eduId);
        }
    }

    @Test
    @DisplayName("Should find all apprentices")
    void findAll_ShouldReturnListOfDomainObjects() {
        try (MockedStatic<ApprenticeJpaMapper> mapper = mockStatic(ApprenticeJpaMapper.class)) {
            ApprenticeEntity e1 = new ApprenticeEntity();
            ApprenticeEntity e2 = new ApprenticeEntity();
            Apprentice d1 = mock(Apprentice.class);
            Apprentice d2 = mock(Apprentice.class);

            when(jpaRepository.findAll()).thenReturn(List.of(e1, e2));
            mapper.when(() -> ApprenticeJpaMapper.toDomain(e1)).thenReturn(d1);
            mapper.when(() -> ApprenticeJpaMapper.toDomain(e2)).thenReturn(d2);

            List<Apprentice> result = adapter.findAll();

            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(d1, d2)));
        }
    }
}
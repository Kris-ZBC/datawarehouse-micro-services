package local.sop.sopinfo.sopeducation.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sopeducation.domain.model.SopEducation;

@DataJpaTest
@TestPropertySource(properties = {
    "security.enabled=false"})
@ActiveProfiles({"test", "h2"})
@Import({SopEducationDomainJpaMapper.class, SopEducationRepositoryAdapter.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SopEducationRepositoryAdapterTest {


    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SopEducationDomainJpaMapper mapper;

    @Autowired
    private SopEducationRepositoryAdapter adapter;

    private static final UUID SOP_REF            = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF  = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final UUID OTHER_SOP_REF       = UUID.fromString("333e4567-e89b-12d3-a456-426614174333");
    private static final UUID OTHER_EDU_REF  = UUID.fromString("444e4567-e89b-12d3-a456-426614174444");

    private static final SopEducationId VALID_ENTITY_ID = new SopEducationId(SOP_REF, EDUCATION_REF);
    private static final CompositeKey VALID_COMPOSITE_KEY   = new CompositeKey(SOP_REF, EDUCATION_REF);


    // ── Helper ─────────────────────────────────────────────────────────────────

    private SopEducationEntity persistEntity(UUID sopRef, UUID educationLineRef, boolean active) {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(new SopEducationId(sopRef, educationLineRef))
                .active(active)
                .build();
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    // ── findById ───────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnDomain_whenEntityExists() {
        persistEntity(SOP_REF, EDUCATION_REF, true);

        Optional<SopEducation> result = adapter.findById(VALID_COMPOSITE_KEY);

        assertTrue(result.isPresent());
        assertEquals(SOP_REF, result.get().getId().key1());
        assertEquals(EDUCATION_REF, result.get().getId().key2());
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityDoesNotExist() {
        Optional<SopEducation> result = adapter.findById(new CompositeKey(OTHER_SOP_REF, OTHER_EDU_REF));

        assertTrue(result.isEmpty());
    }

    // ── save ───────────────────────────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnDomain() {
        SopEducation domain = mapper.toDomain(
                SopEducationEntity.builder()
                        .id(VALID_ENTITY_ID)
                        .active(true)
                        .build());

        SopEducation result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(SOP_REF, result.getId().key1());
        assertEquals(EDUCATION_REF, result.getId().key2());
        assertTrue(result.isActive());
    }

    @Test
    void save_shouldPersistWithCorrectCreatedAt() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        SopEducation domain = mapper.toDomain(
                SopEducationEntity.builder()
                        .id(VALID_ENTITY_ID)
                        .active(true)
                        .build());

        SopEducation result = adapter.save(domain);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(result.getCreatedAt().value().isAfter(before));
        assertTrue(result.getCreatedAt().value().isBefore(after));
    }
    

    // ── update ─────────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateActive_whenEntityExists() {
        persistEntity(SOP_REF, EDUCATION_REF, true);
        entityManager.clear();

        SopEducation updated = mapper.toDomain(
                SopEducationEntity.builder()
                        .id(VALID_ENTITY_ID)
                        .active(false)
                        .build());

        adapter.update(updated);
        entityManager.flush();
        entityManager.clear();

        Optional<SopEducation> result = adapter.findById(VALID_COMPOSITE_KEY);
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void update_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        SopEducation domain = mapper.toDomain(
                SopEducationEntity.builder()
                        .id(new SopEducationId(OTHER_SOP_REF, OTHER_EDU_REF))
                        .active(false)
                        .build());

        var ex = assertThrows(NotFoundException.class, () -> adapter.update(domain));
        assertEquals("key.not.found", ex.getMessage());
    }

    // ── findBySopRef ───────────────────────────────────────────────────────────

    @Test
    void findBySopRef_shouldReturnAllMatchingEntities() {
        persistEntity(SOP_REF, EDUCATION_REF, true);
        persistEntity(SOP_REF, OTHER_EDU_REF, false);
        persistEntity(OTHER_SOP_REF, EDUCATION_REF, true);

        List<SopEducation> result = adapter.findBySopRef(SOP_REF);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getId().key1().equals(SOP_REF)));
    }

    @Test
    void findBySopRef_shouldReturnEmptyList_whenNoMatchFound() {
        persistEntity(SOP_REF, EDUCATION_REF, true);

        List<SopEducation> result = adapter.findBySopRef(OTHER_SOP_REF);

        assertTrue(result.isEmpty());
    }

    // ── findByEducationLineRef ─────────────────────────────────────────────────

    @Test
    void findByEducationLineRef_shouldReturnAllMatchingEntities() {
        persistEntity(SOP_REF, EDUCATION_REF, true);
        persistEntity(OTHER_SOP_REF, EDUCATION_REF, false);
        persistEntity(SOP_REF, OTHER_EDU_REF, true);

        List<SopEducation> result = adapter.findByEducationRef(EDUCATION_REF);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getId().key2().equals(EDUCATION_REF)));
    }

    @Test
    void findByEducationLineRef_shouldReturnEmptyList_whenNoMatchFound() {
        persistEntity(SOP_REF, EDUCATION_REF, true);

        List<SopEducation> result = adapter.findByEducationRef(OTHER_EDU_REF);

        assertTrue(result.isEmpty());
    }
}

package local.sop.datawarehouse.sopeducation.interfaceadapters.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import local.sop.common.libs.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.datawarehouse.sopeducation.domain.model.SopEducation;
import local.sop.datawarehouse.sopeducation.domain.model.valueobjects.CreatedAtTimestamp;

class SopEducationDomainJpaMapperTest {

    private SopEducationDomainJpaMapper mapper;

    private static final UUID SOP_REF            = UUID.fromString("111e4567-e89b-12d3-a456-426614174111");
    private static final UUID EDUCATION_REF  = UUID.fromString("222e4567-e89b-12d3-a456-426614174222");
    private static final SopEducationId ENTITY_ID    = new SopEducationId(SOP_REF, EDUCATION_REF);
    private static final CompositeKey COMPOSITE_KEY      = new CompositeKey(SOP_REF, EDUCATION_REF);

    
    @BeforeEach
    void setUp() {
        mapper = new SopEducationDomainJpaMapper();
    }

    // ── toDomain ───────────────────────────────────────────────────────────────

    @Test
    void toDomain_shouldMapIdCorrectly() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(ENTITY_ID)
                .active(true)
                .build();

        SopEducation domain = mapper.toDomain(entity);

        assertEquals(SOP_REF, domain.getId().key1());
        assertEquals(EDUCATION_REF, domain.getId().key2());
    }

    @Test
    void toDomain_shouldMapActiveCorrectly() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(ENTITY_ID)
                .active(false)
                .build();

        SopEducation domain = mapper.toDomain(entity);

        assertFalse(domain.isActive());
    }

    @Test
    void toDomain_shouldMapCreatedAtCorrectly() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(ENTITY_ID)
                .active(true)
                .build();

        SopEducation domain = mapper.toDomain(entity);

        assertNotNull(domain.getCreatedAt());
        assertEquals(entity.getCreatedAt(), domain.getCreatedAt().value());
    }

    // ── toEntity ───────────────────────────────────────────────────────────────

    @Test
    void toEntity_shouldMapIdCorrectly() {
        SopEducation domain = SopEducation.builder()
                .id(COMPOSITE_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        SopEducationEntity entity = mapper.toEntity(domain);

        assertEquals(SOP_REF, entity.getId().getSopRef());
        assertEquals(EDUCATION_REF, entity.getId().getEducationRef());
    }

    @Test
    void toEntity_shouldMapActiveCorrectly() {
        SopEducation domain = SopEducation.builder()
                .id(COMPOSITE_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        SopEducationEntity entity = mapper.toEntity(domain);

        assertFalse(entity.isActive());
    }

    // ── updateEntity ───────────────────────────────────────────────────────────

    @Test
    void updateEntity_shouldUpdateActiveToTrue() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(ENTITY_ID)
                .active(false)
                .build();

        SopEducation domain = SopEducation.builder()
                .id(COMPOSITE_KEY)
                .active(true)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        mapper.updateEntity(domain, entity);

        assertTrue(entity.isActive());
    }

    @Test
    void updateEntity_shouldUpdateActiveToFalse() {
        SopEducationEntity entity = SopEducationEntity.builder()
                .id(ENTITY_ID)
                .active(true)
                .build();

        SopEducation domain = SopEducation.builder()
                .id(COMPOSITE_KEY)
                .active(false)
                .createdAt(new CreatedAtTimestamp(LocalDateTime.now()))
                .build();

        mapper.updateEntity(domain, entity);

        assertFalse(entity.isActive());
    }

    // ── roundtrip ──────────────────────────────────────────────────────────────

    @Test
    void toDomain_andBack_shouldPreserveId() {
        SopEducationEntity original = SopEducationEntity.builder()
                .id(ENTITY_ID)
                .active(true)
                .build();

        SopEducation domain = mapper.toDomain(original);
        SopEducationEntity entity = mapper.toEntity(domain);

        assertEquals(original.getId().getSopRef(), entity.getId().getSopRef());
        assertEquals(original.getId().getEducationRef(), entity.getId().getEducationRef());
        assertEquals(original.isActive(), entity.isActive());
    }

    @Test
void toEntity_shouldNeverSetCreatedAt() {
    SopEducation domain = SopEducation.builder()
            .id(COMPOSITE_KEY)
            .active(true)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(5)))
            .build();

    SopEducationEntity entity = mapper.toEntity(domain);

    // createdAt on entity is always set by the entity constructor, never by the mapper
    // so it should reflect NOW, not the domain value
    assertNotEquals(domain.getCreatedAt().value(), entity.getCreatedAt());
}

@Test
void updateEntity_shouldNeverUpdateCreatedAt() {
    SopEducationEntity entity = SopEducationEntity.builder()
            .id(ENTITY_ID)
            .active(true)
            .build();

    LocalDateTime originalCreatedAt = entity.getCreatedAt();

    SopEducation domain = SopEducation.builder()
            .id(COMPOSITE_KEY)
            .active(false)
            .createdAt(new CreatedAtTimestamp(LocalDateTime.now().minusDays(5)))
            .build();

    mapper.updateEntity(domain, entity);

    assertEquals(originalCreatedAt, entity.getCreatedAt());
}
}

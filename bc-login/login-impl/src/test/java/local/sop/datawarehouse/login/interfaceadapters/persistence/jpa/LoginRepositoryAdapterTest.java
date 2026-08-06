package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.valueobjects.HashedPassword;
import local.sop.datawarehouse.login.domain.model.valueobjects.LoginId;
import local.sop.datawarehouse.login.domain.model.valueobjects.PersonRef;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;

@ExtendWith(MockitoExtension.class)
class LoginRepositoryAdapterTest {

    @Mock
    private LoginSpringDataRepository jpaRepo;

    @Mock
    private LoginMapper mapper;

    private LoginRepositoryAdapter adapter;

    private static final String VALID_HASH = "$2b$10$" + "a".repeat(53);
    private static final String USERNAME   = "nick579a@zbc.dk";

    @BeforeEach
    void setUp() {
        adapter = new LoginRepositoryAdapter(mapper, jpaRepo);
    }

    // ── save ───────────────────────────────────────────────────────────────────

    @Test
    void shouldSaveLogin_andReturnMappedDomainObject() {
        UUID id = UUID.randomUUID();

        Login domain = Login.builder()
            .id(new LoginId(id))
            .personRef(new PersonRef(UUID.randomUUID()))
            .username(new Username(USERNAME))
            .password(HashedPassword.of(VALID_HASH))
            .status(LoginStatus.ACTIVATED)
            .build();

        LoginEntity entity = LoginEntity.builder()
            .id(id)
            .personRef(domain.getPersonRef().value())
            .username(domain.getUsername().value())
            .password(VALID_HASH)
            .status(LoginStatus.ACTIVATED)
            .build();

        when(mapper.toEntity(any(Login.class))).thenReturn(entity);
        when(jpaRepo.save(any(LoginEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Login result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(id, result.getId().value());
        verify(mapper).toEntity(any());
        verify(jpaRepo, times(1)).save(any());
        verify(mapper).toDomain(any());
        verifyNoMoreInteractions(jpaRepo, mapper);
    }

    // ── findByUsername ─────────────────────────────────────────────────────────

    @Test
    void shouldFindLoginByUsername_whenExists() {
        LoginEntity entity = LoginEntity.builder()
            .id(UUID.randomUUID())
            .personRef(UUID.randomUUID())
            .username(USERNAME)
            .password(VALID_HASH)
            .status(LoginStatus.ACTIVATED)
            .build();

        Login domain = Login.builder()
            .personRef(new PersonRef(entity.getPersonRef()))
            .username(new Username(entity.getUsername()))
            .password(HashedPassword.of(VALID_HASH))
            .status(entity.getStatus())
            .build();

        when(jpaRepo.findByUsername(USERNAME)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Login result = adapter.findByUsername(new Username(USERNAME));

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername().value());
        verify(jpaRepo).findByUsername(USERNAME);
        verify(mapper).toDomain(entity);
        verifyNoMoreInteractions(jpaRepo, mapper);
    }

    @Test
    void shouldReturnNull_whenLoginNotFound() {
        when(jpaRepo.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Login result = adapter.findByUsername(new Username(USERNAME));

        assertNull(result);
        verify(jpaRepo).findByUsername(USERNAME);
        verifyNoMoreInteractions(jpaRepo);
    }

    // ── compensate ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnTrue_whenLoginExistsAndDeleteSucceeds() {
        UUID id = UUID.randomUUID();
        LoginId loginId = LoginId.of(id);

        Login domain = Login.builder()
            .id(loginId)
            .personRef(new PersonRef(UUID.randomUUID()))
            .username(new Username(USERNAME))
            .password(HashedPassword.of(VALID_HASH))
            .status(LoginStatus.ACTIVATED)
            .build();

        LoginEntity entity = LoginEntity.builder()
            .id(id)
            .personRef(domain.getPersonRef().value())
            .username(USERNAME)
            .password(VALID_HASH)
            .status(LoginStatus.ACTIVATED)
            .build();

        when(jpaRepo.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);
        when(jpaRepo.delete(id)).thenReturn(1);

        Boolean result = adapter.compensate(loginId, SagaOutcome.COMPENSATED);

        assertTrue(result);
        verify(jpaRepo).findById(id);
        verify(jpaRepo).delete(id);
    }

    @Test
    void shouldReturnFalse_whenLoginNotFound() {
        UUID id = UUID.randomUUID();
        LoginId loginId = LoginId.of(id);

        when(jpaRepo.findById(id)).thenReturn(Optional.empty());

        Boolean result = adapter.compensate(loginId, SagaOutcome.COMPENSATED);

        assertFalse(result);
        verify(jpaRepo).findById(id);
        verify(jpaRepo, never()).delete(any(UUID.class));
    }

    @Test
    void shouldThrowConflictException_whenSagaOutcomeIsNotCompensated() {
        UUID id = UUID.randomUUID();
        LoginId loginId = LoginId.of(id);

        assertThrows(ConflictException.class, () ->
            adapter.compensate(loginId, SagaOutcome.IDEMPOTENT));

        verify(jpaRepo, never()).findById(any());
        verify(jpaRepo, never()).delete(any(UUID.class));
    }
}
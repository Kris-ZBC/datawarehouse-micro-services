package local.sop.datawarehouse.login.interfaceadapters.persistence.jpa;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.login.domain.model.valueobjects.Username;
import local.sop.datawarehouse.login.domain.ports.out.LoginRepositoryPort;
import local.sop.common.libs.sharedkernel.exceptions.ConflictException;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

@Repository
public class LoginRepositoryAdapter implements LoginRepositoryPort {
	private static final Logger log = LoggerFactory.getLogger(LoginRepositoryAdapter.class);
	private final LoginMapper mapper;
	private final LoginSpringDataRepository repo;

	public LoginRepositoryAdapter(LoginMapper mapper, LoginSpringDataRepository repo) {
		this.mapper = mapper;
		this.repo = repo;
	}

	@Override
	public Login save(Login login) {
		LoginEntity entity = mapper.toEntity(login);
		entity = repo.save(entity);
		log.info("saved record with key {}", entity.getId().toString());
		return mapper.toDomain(entity);
	}

	@Override
	public Login findByUsername(Username username) {
		return repo.findByUsername(username.value())
			.map(mapper::toDomain)
			.orElse(null);
	}

	@Override
	public Optional<Login> findById(DomainId id) {
        return repo.findById(id.value())
                .map(mapper::toDomain);
	}

	@Override
    public Boolean compensate(DomainId id, SagaOutcome sagaState) {
		if (sagaState != SagaOutcome.COMPENSATED) {
				throw new ConflictException("compensate.wrong_state",
					Map.of("expected", SagaOutcome.COMPENSATED.name(),
						"actual", sagaState.name()));
		}
		Optional<Login> found = findById(id);
		if (found.isEmpty()) {
			return false;
		}
		return repo.delete(id.value()) == 1;
	}
}

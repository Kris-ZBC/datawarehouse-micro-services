package local.sop.sopinfo.login.domain.ports.out;

import java.util.Optional;

import local.sop.sopinfo.login.domain.model.Login;
import local.sop.sopinfo.login.domain.model.valueobjects.Username;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;
import local.sop.common.libs.sharedkernel.valueobjects.DomainId;

public interface LoginRepositoryPort {
	Login save(Login login);
	Login findByUsername(Username username);
	Optional<Login> findById(DomainId id);
	Boolean compensate(DomainId loginId, SagaOutcome sagaState);
}

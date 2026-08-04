package local.sop.sopinfo.login.interfaceadapters.persistence.jpa;


import java.util.UUID;

import org.springframework.stereotype.Component;

import local.sop.sopinfo.login.domain.ports.out.LoginRepositoryPort;
import local.sop.common.libs.sharedkernel.enums.LoginStatus;
import local.sop.common.libs.sharedkernel.login.ValidateTechUser;
import local.sop.sopinfo.login.domain.model.valueobjects.LoginId;

@Component
public class TechUserValidatorAdapter implements ValidateTechUser {

	private static final UUID techUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private final LoginRepositoryPort loginRepositoryPort;

	public TechUserValidatorAdapter(LoginRepositoryPort loginRepositoryPort) {
		this.loginRepositoryPort = loginRepositoryPort;
	}

    @Override
    public boolean isTechUser(UUID loginId) {
        return techUserId.equals(loginId);
    }

	@Override
	public boolean isActivated(UUID loginId) {
		return loginRepositoryPort.findById(LoginId.of(loginId))
			.map(login -> LoginStatus.ACTIVATED.equals(login.getStatus()))
			.orElse(false);
	}
}

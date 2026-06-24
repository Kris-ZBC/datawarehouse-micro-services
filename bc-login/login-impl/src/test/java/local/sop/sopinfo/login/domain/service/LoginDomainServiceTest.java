package local.sop.sopinfo.login.domain.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.sopinfo.login.domain.model.Login;
import local.sop.sopinfo.sharedkernel.enums.LoginStatus;

public class LoginDomainServiceTest {
	@Test
	void shouldCreateLogin_whenValidInput() {
		LoginDomainService service = new LoginDomainService();

		Login login = service.createLogin(
			UUID.randomUUID(),
			"nick579a@zbc.dk",
			"$2b$10$" + "a".repeat(53),  // bcrypt hash
			LoginStatus.ACTIVATED
		);

		assertNotNull(login);
	}
}

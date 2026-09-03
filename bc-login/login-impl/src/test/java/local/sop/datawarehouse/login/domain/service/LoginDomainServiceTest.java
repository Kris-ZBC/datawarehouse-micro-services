package local.sop.datawarehouse.login.domain.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.login.domain.model.Login;
import local.sop.datawarehouse.sharedlib.enums.LoginStatus;

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

package local.sop.sopinfo.login.saga.application.interfaceweb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import local.sop.sopinfo.login.saga.application.api.LoginSagaDirectory;
import local.sop.sopinfo.login.saga.application.api.dto.LoginCmd;
import local.sop.sopinfo.login.saga.application.api.dto.LoginResult;

@RestController
@RequestMapping("/internal/saga/logins")
public class LoginSagaController {
	
	private LoginSagaDirectory directory;

	LoginSagaController(LoginSagaDirectory directory) {
		this.directory = directory;
	}

	@PostMapping("/sessions/login")
	public ResponseEntity<LoginResult> login(@Valid @RequestBody LoginCmd cmd) {
		LoginResult result = directory.login(cmd);
		return ResponseEntity.ok(result);
	}
}

package local.sop.datawarehouse.sharedlib.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public enum LoginStatus {
	ACTIVATED, DEACTIVATED;

	public static LoginStatus parse(String status) throws ValidationException {
		Logger log = LoggerFactory.getLogger(LoginStatus.class);

		if(status == null) {
			log.warn("status is null!");
			throw new ValidationException("login.status.invalid", Map.of("field", "login-status"));
		}

		try {
			return valueOf(status);
		}
		catch(IllegalArgumentException ex) {
			log.warn("status is invalid {}", status);
			throw new ValidationException("login.status.invalid", Map.of("field", "login-status"));
		}
	}
}

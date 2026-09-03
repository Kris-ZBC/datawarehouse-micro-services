package local.sop.datawarehouse.login.application.api.dto;

import java.util.UUID;

public record AuthenticationResult(
	UUID loginId,
	UUID personRef,
	String username
) {}

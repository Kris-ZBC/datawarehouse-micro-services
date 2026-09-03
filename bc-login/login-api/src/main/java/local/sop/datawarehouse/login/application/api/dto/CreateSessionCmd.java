package local.sop.datawarehouse.login.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateSessionCmd(
	@NotNull(message="{login.loginid.invalid}") UUID loginId,
	@NotNull(message="{login.role.invalid}") String role
) {}
package local.sop.datawarehouse.login.application.api.dto;

import jakarta.validation.constraints.NotNull;

public record AuthenticateCmd(
    @NotNull(message="{login.credentials.invalid}") String username,
	@NotNull(message="{login.credentials.invalid}") String password
) {}


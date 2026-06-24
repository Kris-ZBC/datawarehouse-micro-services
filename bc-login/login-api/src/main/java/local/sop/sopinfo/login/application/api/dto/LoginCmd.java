package local.sop.sopinfo.login.application.api.dto;

import jakarta.validation.constraints.NotNull;

public record LoginCmd(
	@NotNull(message="{login.credentials.invalid}") String username,
	@NotNull(message="{login.credentials.invalid}") String password
) {}

package local.sop.datawarehouse.login.application.api.dto;

import jakarta.validation.constraints.NotNull;

public record LogoutCmd(
	@NotNull(message="{session.token.invalid}") String sessionToken
) {}

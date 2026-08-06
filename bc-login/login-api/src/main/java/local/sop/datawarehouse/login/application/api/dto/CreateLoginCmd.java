package local.sop.datawarehouse.login.application.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLoginCmd(
	@NotNull(message="{login.personRef.invalid}") UUID personRef,
	@NotBlank(message="{login.username.invalid}") String username,
	@NotBlank(message="{login.status.invalid}") String status
) {}

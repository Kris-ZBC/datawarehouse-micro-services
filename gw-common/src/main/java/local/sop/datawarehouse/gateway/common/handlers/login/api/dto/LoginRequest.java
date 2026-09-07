package local.sop.datawarehouse.gateway.common.handlers.login.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "{login.username.invalid}") String username,
    @NotBlank(message = "{login.password.invalid}") String password
) {}

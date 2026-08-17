package local.sop.datawarehouse.login.application.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateSessionCmd(
    @NotNull(message = "{session.token.required}")
    @NotBlank(message = "{session.token.required}")
    String sessionToken
) {}

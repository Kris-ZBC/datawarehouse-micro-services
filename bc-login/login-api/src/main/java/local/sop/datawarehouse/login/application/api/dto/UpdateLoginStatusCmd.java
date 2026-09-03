package local.sop.datawarehouse.login.application.api.dto;

import java.util.UUID;
 
import jakarta.validation.constraints.NotNull;
 
// CHANGED: status is now a plain String, matching CreateLoginCmd's
// existing pattern for the same field — LoginStatus.parse() converts
// it inside LoginApplicationService, not at the DTO itself.
public record UpdateLoginStatusCmd(
	@NotNull(message="{login.loginid.invalid}") UUID loginId,
	@NotNull(message="{login.status.invalid}") String status
) {}
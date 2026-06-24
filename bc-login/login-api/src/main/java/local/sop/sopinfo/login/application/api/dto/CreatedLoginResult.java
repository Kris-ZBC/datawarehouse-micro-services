package local.sop.sopinfo.login.application.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatedLoginResult(UUID id, String password, LocalDateTime createdAt) {

}

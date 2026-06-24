package local.sop.sopinfo.person.application.api.dto;

import jakarta.validation.constraints.Email;

public record UpdatePersonCmd(
        String firstName,
        String lastName,
        @Email String email
) {
}

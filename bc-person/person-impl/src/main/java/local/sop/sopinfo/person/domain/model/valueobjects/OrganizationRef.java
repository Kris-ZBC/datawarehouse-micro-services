package local.sop.sopinfo.person.domain.model.valueobjects;

import java.util.Map;
import java.util.UUID;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.sharedkernel.valueobjects.DomainId;

public record OrganizationRef(UUID value) implements DomainId {

    public OrganizationRef {
        if (value == null) {
            throw new ValidationException("person.organizationref.blank", Map.of("field", "organizationRef"));
        }
    }

    public static OrganizationRef fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("person.organizationref.blank", Map.of("field", "organizationRef"));
        }

        try {
            return new OrganizationRef(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("person.organizationref.invalid", Map.of("field", "organizationRef"));
        }
    }
}
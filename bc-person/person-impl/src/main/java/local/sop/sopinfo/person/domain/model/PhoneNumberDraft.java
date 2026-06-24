package local.sop.sopinfo.person.domain.model;

import local.sop.sopinfo.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.sopinfo.sharedkernel.enums.PhoneUserType;

public record PhoneNumberDraft(
        PhoneUserType type,
        PhoneNumberValue value
) {
}
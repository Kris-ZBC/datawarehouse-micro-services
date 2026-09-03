package local.sop.datawarehouse.person.domain.model;

import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;
import local.sop.datawarehouse.sharedlib.enums.PhoneUserType;

public record PhoneNumberDraft(
        PhoneUserType type,
        PhoneNumberValue value
) {
}
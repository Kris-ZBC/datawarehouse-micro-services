package local.sop.datawarehouse.person.domain.model;

import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import local.sop.datawarehouse.person.domain.model.valueobjects.PhoneNumberValue;

public record PhoneNumberDraft(
        PhoneUserType type,
        PhoneNumberValue value
) {
}
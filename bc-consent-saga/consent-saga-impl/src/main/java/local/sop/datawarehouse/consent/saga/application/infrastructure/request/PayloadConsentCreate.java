package local.sop.datawarehouse.consent.saga.application.infrastructure.request;

import local.sop.common.libs.sharedkernel.enums.ConsentPurpose;
import local.sop.common.libs.sharedkernel.enums.ConsentType;

public record PayloadConsentCreate(
    boolean active, String text, ConsentPurpose purpose, ConsentType type
) {

}

package local.sop.datawarehouse.consent.saga.application.infrastructure.request;

import local.sop.datawarehouse.sharedlib.enums.ConsentPurpose;
import local.sop.datawarehouse.sharedlib.enums.ConsentType;

public record PayloadConsentCreate(
    boolean active, String text, ConsentPurpose purpose, ConsentType type
) {

}

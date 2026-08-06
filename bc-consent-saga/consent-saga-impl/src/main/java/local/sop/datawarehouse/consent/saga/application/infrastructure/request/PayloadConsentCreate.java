package local.sop.datawarehouse.consent.saga.application.infrastructure.request;

public record PayloadConsentCreate(
    boolean active, String text
) {

}

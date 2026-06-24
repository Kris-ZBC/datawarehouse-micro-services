package local.sop.sopinfo.consent.saga.application.infrastructure.request;

public record PayloadConsentCreate(
    boolean active, String text
) {

}

package local.sop.datawarehouse.consent.interfaceadapters.bootstrap;

import java.util.UUID;

public final class WellKnownConsentStatements {
    /** "I agree to the Terms & Conditions" — REQUIRED_SERVICE / REQUIRED. */
    public static final UUID TERMS_AND_CONDITIONS = UUID.fromString("00000000-0000-0000-0000-000000000001");
 
    /** "I agree to receive notifications" — COMMUNICATION / OPTIONAL. */
    public static final UUID ALLOW_NOTIFICATIONS = UUID.fromString("00000000-0000-0000-0000-000000000002");
 
    private WellKnownConsentStatements() {
    }
}

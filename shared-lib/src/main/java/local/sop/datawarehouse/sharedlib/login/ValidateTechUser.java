package local.sop.datawarehouse.sharedlib.login;

import java.util.UUID;

public interface ValidateTechUser {
    boolean isTechUser(UUID LoginId);
	boolean isActivated(UUID LoginId);

}

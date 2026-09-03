package local.sop.datawarehouse.login.saga.application.infrastructure.request;

import java.util.UUID;
 
import local.sop.datawarehouse.sharedlib.enums.UserRole;
 
public record PayloadCreateSession(
	UUID loginId,
	UserRole role
) { }

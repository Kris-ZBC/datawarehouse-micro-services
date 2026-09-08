package local.sop.datawarehouse.login.saga.application.infrastructure.request;

import java.util.UUID;

import local.sop.common.libs.sharedkernel.enums.UserRole;
 
 
public record PayloadCreateSession(
	UUID loginId,
	UserRole role
) { }

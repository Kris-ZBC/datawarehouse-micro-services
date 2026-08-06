package local.sop.datawarehouse.login.saga.application.infrastructure.request;

public record PayloadLogout(
	String sessionToken
) { }

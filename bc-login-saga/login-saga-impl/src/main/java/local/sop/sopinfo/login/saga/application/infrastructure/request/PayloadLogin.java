package local.sop.datawarehouse.login.saga.application.infrastructure.request;

public record PayloadLogin(
	String username,
	String password
) { }

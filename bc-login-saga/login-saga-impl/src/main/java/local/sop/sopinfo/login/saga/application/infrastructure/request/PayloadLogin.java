package local.sop.sopinfo.login.saga.application.infrastructure.request;

public record PayloadLogin(
	String username,
	String password
) { }

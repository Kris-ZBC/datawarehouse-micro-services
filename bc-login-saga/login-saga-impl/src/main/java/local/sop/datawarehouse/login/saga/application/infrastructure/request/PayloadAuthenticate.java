package local.sop.datawarehouse.login.saga.application.infrastructure.request;

// CHANGED: was PayloadLogin — renamed to match what it actually does
// now (the authenticate step only), since login no longer happens in
// one call.
public record PayloadAuthenticate(
	String username,
	String password
) { }

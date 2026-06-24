package local.sop.sopinfo.login.domain.service;

import java.util.UUID;

import local.sop.sopinfo.login.domain.model.Login;
import local.sop.sopinfo.sharedkernel.enums.LoginStatus;

public interface LoginDomain {
	Login createLogin(UUID personRef, String username, String password, LoginStatus status);
}

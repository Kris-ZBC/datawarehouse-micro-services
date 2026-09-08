package local.sop.datawarehouse.login.saga.application.ports.out;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import local.sop.datawarehouse.login.saga.application.api.dto.LoginResult;
import local.sop.datawarehouse.login.saga.application.infrastructure.response.ResponseCompensated;
import local.sop.datawarehouse.login.saga.application.ports.out.auditlog.AuditlogPort;
import local.sop.datawarehouse.login.saga.application.ports.out.consent.ConsentPort;
import local.sop.datawarehouse.login.saga.application.ports.out.login.LoginPort;
import local.sop.datawarehouse.sharedlib.enums.ActorType;
import local.sop.datawarehouse.sharedlib.enums.Severity;
import local.sop.common.libs.sharedkernel.enums.UserRole;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

class PortsOutContractTest {

    // CHANGED: LoginPort no longer has login(String,String) or
    // compensate(UUID,Class,SagaOutcome) — see LoginPort's own Javadoc
    // for why (split into authenticate()/createSession(), compensate
    // replaced by logout() since authenticate/createSession create
    // nothing for a generic id-based compensate to target).
    @Test
    void loginPort_shouldExposeExpectedContract() throws Exception {
        Method authenticate = LoginPort.class.getMethod("authenticate", String.class, String.class);
        assertEquals(LoginPort.AuthenticationResult.class, authenticate.getReturnType());

        Method createSession = LoginPort.class.getMethod("createSession", UUID.class, UserRole.class);
        assertEquals(LoginResult.class, createSession.getReturnType());

        Method logout = LoginPort.class.getMethod("logout", String.class);
        assertEquals(void.class, logout.getReturnType());
    }

    @Test
    void auditlogPort_shouldExposeExpectedContract() throws Exception {
        Method create = AuditlogPort.class.getMethod(
                "create",
                UUID.class,
                ActorType.class,
                Severity.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class);
        assertEquals(UUID.class, create.getReturnType());

        Method compensate = AuditlogPort.class.getMethod("compensate", UUID.class, Class.class, SagaOutcome.class);
        assertEquals(ResponseCompensated.class, compensate.getReturnType());
    }

    @Test
    void consentPort_shouldExposeExpectedContract() throws Exception {
        Method hasConsent = ConsentPort.class.getMethod("hasConsent", UUID.class);
        assertEquals(boolean.class, hasConsent.getReturnType());

        Method compensate = ConsentPort.class.getMethod("compensate", UUID.class, Class.class, SagaOutcome.class);
        assertEquals(ResponseCompensated.class, compensate.getReturnType());
    }
}
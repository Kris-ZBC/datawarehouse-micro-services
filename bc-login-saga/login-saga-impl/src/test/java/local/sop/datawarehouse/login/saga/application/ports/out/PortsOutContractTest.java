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
import local.sop.common.libs.sharedkernel.enums.ActorType;
import local.sop.common.libs.sharedkernel.enums.Severity;
import local.sop.common.libs.sharedkernel.sagas.compensate.enums.SagaOutcome;

class PortsOutContractTest {

    @Test
    void loginPort_shouldExposeExpectedContract() throws Exception {
        Method login = LoginPort.class.getMethod("login", String.class, String.class);
        assertEquals(LoginResult.class, login.getReturnType());

        Method compensate = LoginPort.class.getMethod("compensate", UUID.class, Class.class, SagaOutcome.class);
        assertEquals(ResponseCompensated.class, compensate.getReturnType());
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

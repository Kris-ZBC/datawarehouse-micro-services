package local.sop.datawarehouse.login.saga.application.ports.out.apprentice;

import java.util.UUID;

public interface ApprenticePort {
    boolean isApprentice(UUID personRef);
}

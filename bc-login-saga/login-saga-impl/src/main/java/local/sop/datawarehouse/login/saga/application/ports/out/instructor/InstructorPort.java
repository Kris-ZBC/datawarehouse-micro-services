package local.sop.datawarehouse.login.saga.application.ports.out.instructor;

import java.util.UUID;

public interface InstructorPort {
    boolean isInstructor(UUID personRef);
}

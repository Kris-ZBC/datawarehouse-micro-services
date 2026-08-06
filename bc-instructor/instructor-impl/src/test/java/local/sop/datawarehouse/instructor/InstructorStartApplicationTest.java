package local.sop.datawarehouse.instructor;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

public class InstructorStartApplicationTest {
    @Test
    void main_ShouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            
            InstructorStartApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                InstructorStartApplication.class,
                new String[]{}
            ));
        }
    }
}

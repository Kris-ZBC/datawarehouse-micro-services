package local.sop.datawarehouse.consent;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

public class ConsentStartApplicationTest {
     @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            ConsentStartApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                ConsentStartApplication.class,
                new String[]{}
            ));
        }
    }
    
}

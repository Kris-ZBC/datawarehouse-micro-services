package local.sop.sopinfo.consent.saga;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

public class ConsentSagaStartApplicationTest {


    @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            ConsentSagaStartApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                ConsentSagaStartApplication.class,
                new String[]{}
            ));
        }
    }
    
}

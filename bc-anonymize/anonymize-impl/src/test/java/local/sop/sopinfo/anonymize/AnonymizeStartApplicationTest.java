package local.sop.sopinfo.anonymize;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;


public class AnonymizeStartApplicationTest {
    @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            AnonymizeStartApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                AnonymizeStartApplication.class,
                new String[]{}
            ));
        }
    }

}

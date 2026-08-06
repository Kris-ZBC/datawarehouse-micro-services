package local.sop.datawarehouse.personnotification;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

public class PersonNotificationStartApplicationTest {

    @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            PersonNotificationStartApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                PersonNotificationStartApplication.class,
                new String[]{}
            ));
        }
    }
    
}

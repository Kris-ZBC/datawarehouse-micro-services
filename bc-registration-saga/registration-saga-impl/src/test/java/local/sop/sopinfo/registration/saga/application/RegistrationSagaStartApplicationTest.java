package local.sop.sopinfo.registration.saga.application;


import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import local.sop.sopinfo.registration.saga.RegistrationSagaStartApplication;

class RegistrationSagaStartApplicationTest {

     @Test
    void main_shouldCallSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            RegistrationSagaStartApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(
                RegistrationSagaStartApplication.class,
                new String[]{}
            ));
        }
    }
}

package local.sop.datawarehouse.education;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class EducationStartApplicationTest {

    @Test
    void mainShouldRun() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(EducationStartApplication.class, new String[]{}))
                    .thenReturn(null);

            EducationStartApplication.main(new String[]{});

            mockedSpringApplication.verify(() -> SpringApplication.run(EducationStartApplication.class, new String[]{}));
        }
    }
}
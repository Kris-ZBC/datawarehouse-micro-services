package local.sop.sopinfo.education.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EducationSagaStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(EducationSagaStartApplication.class, args);
    }
}
package local.sop.sopinfo.educationline.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EducationLineSagaStartApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(EducationLineSagaStartApplication.class, args);
    }
}

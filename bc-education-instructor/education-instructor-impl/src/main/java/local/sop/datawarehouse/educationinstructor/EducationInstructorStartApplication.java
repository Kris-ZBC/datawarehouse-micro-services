package local.sop.datawarehouse.educationinstructor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EducationInstructorStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(EducationInstructorStartApplication.class, args);
	}
}

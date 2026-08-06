package local.sop.datawarehouse.sopinstructor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SopInstructorStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(SopInstructorStartApplication.class, args);
	}
}

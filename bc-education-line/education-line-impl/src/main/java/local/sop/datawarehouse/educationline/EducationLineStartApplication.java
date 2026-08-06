package local.sop.datawarehouse.educationline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EducationLineStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(EducationLineStartApplication.class, args);
	}
}

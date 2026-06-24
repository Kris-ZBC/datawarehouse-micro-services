package local.sop.sopinfo.registration.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RegistrationSagaStartApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistrationSagaStartApplication.class);
	}
}
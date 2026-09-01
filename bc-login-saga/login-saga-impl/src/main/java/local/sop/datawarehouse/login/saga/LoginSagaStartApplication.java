package local.sop.datawarehouse.login.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoginSagaStartApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginSagaStartApplication.class);
	}
}

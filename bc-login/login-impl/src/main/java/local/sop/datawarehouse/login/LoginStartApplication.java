package local.sop.datawarehouse.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoginStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(LoginStartApplication.class, args);
	}
}

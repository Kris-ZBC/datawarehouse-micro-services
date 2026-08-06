package local.sop.datawarehouse.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificationStartApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationStartApplication.class, args);
	}
}

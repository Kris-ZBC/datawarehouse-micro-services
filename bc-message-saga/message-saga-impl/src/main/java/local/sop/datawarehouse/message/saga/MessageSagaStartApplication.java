package local.sop.datawarehouse.message.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MessageSagaStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(MessageSagaStartApplication.class, args);
	}
}

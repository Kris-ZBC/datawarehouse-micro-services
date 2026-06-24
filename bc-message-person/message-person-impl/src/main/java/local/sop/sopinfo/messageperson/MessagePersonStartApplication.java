package local.sop.sopinfo.messageperson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MessagePersonStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(MessagePersonStartApplication.class, args);
	}
}

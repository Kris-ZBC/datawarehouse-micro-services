package local.sop.datawarehouse.anonymize.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan

public class AnonymizeSagaStartApplication {
	public static void main(String[] args) {
		SpringApplication.run(AnonymizeSagaStartApplication.class);
	}
}

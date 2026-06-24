package local.sop.sopinfo.consent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConsentStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsentStartApplication.class, args);
    }
}
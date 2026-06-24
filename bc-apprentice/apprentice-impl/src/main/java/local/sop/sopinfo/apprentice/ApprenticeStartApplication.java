package local.sop.sopinfo.apprentice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApprenticeStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApprenticeStartApplication.class, args);
    }
}

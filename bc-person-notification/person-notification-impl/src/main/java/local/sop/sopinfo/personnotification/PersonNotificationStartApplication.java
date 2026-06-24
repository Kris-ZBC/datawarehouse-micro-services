package local.sop.sopinfo.personnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan

public class PersonNotificationStartApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonNotificationStartApplication.class, args);
    }
}
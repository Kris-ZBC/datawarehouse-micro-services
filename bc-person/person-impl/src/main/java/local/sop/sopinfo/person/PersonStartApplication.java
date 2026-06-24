
package local.sop.sopinfo.person;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PersonStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonStartApplication.class, args);
    }
}

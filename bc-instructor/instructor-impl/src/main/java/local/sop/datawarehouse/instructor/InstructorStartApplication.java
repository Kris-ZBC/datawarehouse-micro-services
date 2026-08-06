package local.sop.datawarehouse.instructor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InstructorStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstructorStartApplication.class, args);
    }
}
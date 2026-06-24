package local.sop.sopinfo.workhour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WorkHourStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkHourStartApplication.class, args);
    }
}
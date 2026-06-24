package local.sop.sopinfo.sopeducation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class SopEducationStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(SopEducationStartApplication.class, args);
    }        

}

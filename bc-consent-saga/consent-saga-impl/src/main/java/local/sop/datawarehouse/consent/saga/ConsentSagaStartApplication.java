package local.sop.datawarehouse.consent.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConsentSagaStartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsentSagaStartApplication.class);
    }
    
}

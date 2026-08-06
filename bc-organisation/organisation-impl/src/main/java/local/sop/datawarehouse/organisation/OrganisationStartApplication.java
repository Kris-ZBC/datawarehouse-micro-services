package local.sop.datawarehouse.organisation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrganisationStartApplication {
    public static void main(String[] args){
        SpringApplication.run(OrganisationStartApplication.class, args);
    }    
}

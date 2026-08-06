package local.sop.datawarehouse.institution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InstitutionStartApplication {
    public static void main(String[] args){
        SpringApplication.run(InstitutionStartApplication.class, args);
    }    
}
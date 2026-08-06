package local.sop.datawarehouse.sop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SOPStartApplication {
    public static void main(String[] args){
        SpringApplication.run(SOPStartApplication.class, args);
    }    
}

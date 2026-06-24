package local.sop.sopinfo.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication
@ConfigurationPropertiesScan
public class MessageStartApplication {
    public static void main(String[] args){
        SpringApplication.run(MessageStartApplication.class, args);
    }   
}


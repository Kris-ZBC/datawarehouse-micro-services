package local.sop.sopinfo.infrastructure.data;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:application-jpa.properties")
public class JpaAutoConfiguration {
    // Tom klasse - kun for at loade properties
}
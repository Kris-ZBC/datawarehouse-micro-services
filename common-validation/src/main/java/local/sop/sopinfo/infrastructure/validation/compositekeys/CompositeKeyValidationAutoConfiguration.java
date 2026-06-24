package local.sop.sopinfo.infrastructure.validation.compositekeys;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@Conditional(CompositeKeyValidationCondition.class)
@PropertySource("classpath:application-validation.properties")

public class CompositeKeyValidationAutoConfiguration {

    @Bean
    CompositeKeyValidationAspect compositeKeyValidationAspect(ApplicationContext context) {
        return new CompositeKeyValidationAspect(context);
    }
}

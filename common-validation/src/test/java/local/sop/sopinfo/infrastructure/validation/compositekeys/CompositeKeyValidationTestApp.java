package local.sop.sopinfo.infrastructure.validation.compositekeys;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;

import static org.mockito.Mockito.mock;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CompositeKeyValidationTestApp {

    @Bean("key1Port")
    CompositeKeyValidator key1Port() {
        return mock(CompositeKeyValidator.class);
    }

    @Bean("key2Port")
    CompositeKeyValidator key2Port() {
        return mock(CompositeKeyValidator.class);
    }
}
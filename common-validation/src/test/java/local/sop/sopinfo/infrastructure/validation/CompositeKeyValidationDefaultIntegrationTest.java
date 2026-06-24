package local.sop.sopinfo.infrastructure.validation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import local.sop.sopinfo.infrastructure.validation.compositekeys.CompositeKeyValidationAspect;
import local.sop.sopinfo.infrastructure.validation.compositekeys.CompositeKeyValidationAutoConfiguration;

class CompositeKeyValidationDefaultIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CompositeKeyValidationAutoConfiguration.class));

    @Test
    void aspect_shouldBeActive_whenPropertyIsTrue() {
        contextRunner
            .withPropertyValues("compositekey.validate.enabled=true")
            .run(ctx -> {
                if (ctx.getBeanNamesForType(CompositeKeyValidationAspect.class).length == 0) {
                    throw new AssertionError(
                        "Expected aspect to be active when compositekey.validate.enabled=true");
                }
            });
    }

    @Test
    void aspect_shouldNotBeActive_whenPropertyIsFalse() {
        contextRunner
            .withPropertyValues("compositekey.validate.enabled=false")
            .run(ctx -> {
                if (ctx.getBeanNamesForType(CompositeKeyValidationAspect.class).length > 0) {
                    throw new AssertionError(
                        "Expected aspect to be inactive when compositekey.validate.enabled=false");
                }
            });
    }

    @Test
    void aspect_shouldNotBeActive_byDefault() {
        contextRunner
            .run(ctx -> {
                if (ctx.getBeanNamesForType(CompositeKeyValidationAspect.class).length > 0) {
                    throw new AssertionError(
                        "Expected aspect inactive by default");
                }
            });
    }
}

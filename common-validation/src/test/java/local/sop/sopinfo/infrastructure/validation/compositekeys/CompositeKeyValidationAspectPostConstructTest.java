package local.sop.sopinfo.infrastructure.validation.compositekeys;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Service;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey;

class CompositeKeyValidationAspectPostConstructTest {

    @Test
    void startup_shouldFail_whenCommandObjectHasMultipleCompositeKeys() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CompositeKeyValidationAutoConfiguration.class))
            .withPropertyValues("compositekey.validate.enabled=true")
            .withBean("key1Port", CompositeKeyValidator.class,
                () -> mock(CompositeKeyValidator.class))
            .withBean("key2Port", CompositeKeyValidator.class,
                () -> mock(CompositeKeyValidator.class))
            .withBean(AmbiguousCommandService.class)
            .run(ctx -> {
                Throwable failure = ctx.getStartupFailure();
                if (failure == null) {
                    throw new AssertionError(
                        "Expected startup to fail due to ambiguous CompositeKey " +
                        "but context started successfully");
                }
                // Unwrap cause — Spring wraps IllegalStateException in BeanCreationException
                Throwable cause = failure;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                if (!(cause instanceof IllegalStateException) ||
                    !cause.getMessage().contains("Ambiguous CompositeKey")) {
                    throw new AssertionError(
                        "Expected IllegalStateException about ambiguous CompositeKey but got: "
                        + cause);
                }
            });
    }

    @Test
    void startup_shouldSucceed_whenCommandObjectHasExactlyOneCompositeKey() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CompositeKeyValidationAutoConfiguration.class))
            .withPropertyValues("compositekey.validate.enabled=true")
            .withBean("key1Port", CompositeKeyValidator.class,
                () -> mock(CompositeKeyValidator.class))
            .withBean("key2Port", CompositeKeyValidator.class,
                () -> mock(CompositeKeyValidator.class))
            .withBean(ValidCommandService.class)
            .run(ctx -> {
                if (ctx.getStartupFailure() != null) {
                    throw new AssertionError(
                        "Expected startup to succeed but failed with: " +
                        ctx.getStartupFailure().getMessage());
                }
            });
    }

    // ── Ambiguous command — two CompositeKey accessors ─────────────────────────
    record AmbiguousCommand(CompositeKey id, CompositeKey otherId, boolean active) {}

    @Service
    static class AmbiguousCommandService {
        @ValidateCompositeKey(ports = {"key1Port", "key2Port"})
        public void execute(AmbiguousCommand command) {}
    }

    // ── Valid command — exactly one CompositeKey accessor ──────────────────────
    record ValidCommand(CompositeKey id, boolean active) {}

    @Service
    static class ValidCommandService {
        @ValidateCompositeKey(ports = {"key1Port", "key2Port"})
        public void execute(ValidCommand command) {}
    }
}
package local.sop.sopinfo.infrastructure.validation.compositekeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.PostConstruct;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.sopinfo.sharedkernel.compositekey.dtos.CompositeKey;
import local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator;
import local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Aspect
public class CompositeKeyValidationAspect {
    private final ApplicationContext context;
    private final Logger log = LoggerFactory.getLogger(CompositeKeyValidationAspect.class);

    public CompositeKeyValidationAspect(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void init() {
        log.info("====================================================");
        log.info(" CompositeKey Validation Aspect is ACTIVE");
        log.info(" Methods annotated with @ValidateCompositeKey will");
        log.info(" have their composite keys validated against the");
        log.info(" configured ports before execution.");
        log.info("----------------------------------------------------");
        log.info(" Checklist for BC developers:");
        log.info(" [1] Annotate use case method with @ValidateCompositeKey");
        log.info("     e.g. @ValidateCompositeKey(key1Port = \"sopPort\", key2Port = \"educationLinePort\")");
        log.info(" [2] Ensure each port interface extends CompositeKeyValidator");
        log.info("     e.g. public interface EducationLinePort extends CompositeKeyValidator");
        log.info(" [3] Ensure each adapter implements exists(UUID id)");
        log.info("     e.g. public boolean exists(UUID id) { return findById(id).isPresent(); }");
        log.info(" [4] Register adapter as a named bean matching key1Port/key2Port");
        log.info("     e.g. @Component(\"educationLinePort\")");
        log.info("====================================================");

        /*
         * Eager startup validation — scan all beans for @ValidateCompositeKey methods
         * and verify their command objects have exactly one CompositeKey accessor.
         * Fails application startup immediately if ambiguous — cannot be bypassed.
         */
        context.getBeansOfType(Object.class).values().forEach(bean ->
            Arrays.stream(AopUtils.getTargetClass(bean).getMethods())
                .filter(m -> m.isAnnotationPresent(ValidateCompositeKey.class))
                .forEach(m -> Arrays.stream(m.getParameterTypes())
                    .filter(p -> !CompositeKey.class.isAssignableFrom(p))
                    .forEach(p -> {
                        long count = Arrays.stream(p.getMethods())
                            .filter(accessor -> accessor.getParameterCount() == 0
                                && CompositeKey.class.isAssignableFrom(accessor.getReturnType()))
                            .count();
                        if (count > 1) {
                            throw new IllegalStateException(
                                "Ambiguous CompositeKey: " + p.getSimpleName() +
                                " has " + count + " CompositeKey accessors. " +
                                "Command objects used with @ValidateCompositeKey " +
                                "must have exactly one CompositeKey.");
                        }
                    })
                )
        );
    }

    /**
     * Intercepts any method annotated with {@link ValidateCompositeKey} and validates
     * each composite key component before the method body executes.
     *
     * <p>The pointcut uses the fully qualified annotation name to avoid classloader
     * binding issues between {@code sharedkernel} and {@code common-validation} —
     * the annotation parameter binding syntax {@code @annotation(varName)} relies on
     * the aspect's classloader being able to resolve the annotation type, which is
     * not guaranteed across library boundaries. Using the fully qualified name in the
     * pointcut expression and reading the annotation via reflection is the safe approach.
     *
     * @param jp the join point providing access to the intercepted method and its arguments
     */
    @Before("@annotation(local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey)")
    public void validateKey(JoinPoint jp ) {
        /*
         * Step 1: Read the @ValidateCompositeKey annotation from the intercepted method
         * via reflection — safe across classloader boundaries between sharedkernel
         * and common-validation.
         */
        ValidateCompositeKey validateCompositeKey =
            jp.getStaticPart().getSignature() instanceof MethodSignature ms
                ? ms.getMethod().getAnnotation(ValidateCompositeKey.class)
                : null;

        // Defensive null check — should never be null since the pointcut matched
        if (validateCompositeKey == null) return;

        /*
         * Step 2: Extract the CompositeKey from the method arguments.
         *
         * Convention A — Direct CompositeKey parameter:
         *   public void create(CompositeKey id) { ... }
         *
         * Convention B — Command object with a no-arg accessor returning CompositeKey:
         *   public void create(CreateSopEducationCmd command) { ... }
         *   The command exposes the key via any no-arg method returning CompositeKey
         *   (e.g. id(), compositeKey(), key()) — not restricted to a specific name.
         *
         * Ambiguity guard: if a command object has more than one CompositeKey accessor,
         * a ValidationException is thrown — the @PostConstruct scan should have caught
         * this at startup, but this is a runtime safety net.
         */
        CompositeKey id = null;
        for (Object arg : jp.getArgs()) {

            // Convention A: argument IS a CompositeKey
            if (arg instanceof CompositeKey key) {
                id = key;
                break;
            }

            // Convention B: argument is a command object — scan all no-arg methods
            // for one that returns CompositeKey, regardless of method name
            if (arg != null) {
                List<CompositeKey> foundKeys = new ArrayList<>();
                for (java.lang.reflect.Method method : arg.getClass().getMethods()) {
                    if (method.getParameterCount() == 0
                            && CompositeKey.class.isAssignableFrom(method.getReturnType())) {
                        try {
                            Object result = method.invoke(arg);
                            if (result instanceof CompositeKey key) {
                                foundKeys.add(key);
                            }
                        } catch (Exception e) {
                            log.debug("Failed to invoke {} on {}: {}",
                                method.getName(),
                                arg.getClass().getSimpleName(),
                                e.getMessage());
                        }
                    }
                }

                // Runtime ambiguity guard — @PostConstruct should have caught this,
                // but guard again here as a safety net
                if (foundKeys.size() > 1) {
                    throw new ValidationException("key.ambiguous",
                        Map.of("field", "id",
                               "class", arg.getClass().getSimpleName(),
                               "count", String.valueOf(foundKeys.size())));
                }

                if (foundKeys.size() == 1) {
                    id = foundKeys.get(0);
                    break;
                }
            }
        }

        /*
         * Step 3: Guard — no CompositeKey found in the arguments.
         * The method was annotated incorrectly. Fail fast with a clear error.
         */
        if (id == null) {
            throw new ValidationException("key.required", Map.of("field", "id"));
        }

        /*
         * Step 4: Validate ports count matches keys count.
         *
         * The annotation declares one port per key — ports[i] validates keys[i].
         * A mismatch means the annotation is misconfigured relative to the CompositeKey.
         * Example: @ValidateCompositeKey(ports = {"sopPort", "educationPort", "institutionPort"})
         * requires a CompositeKey with exactly 3 keys.
         */
        String[] ports = validateCompositeKey.ports();
        UUID[] keys = id.keys();

        if (ports.length != keys.length) {
            throw new ValidationException("key.portmismatch",
                Map.of("ports", String.valueOf(ports.length),
                       "keys",  String.valueOf(keys.length)));
        }

        /*
         * Step 5: Validate each key against its corresponding port — by index.
         *
         * Fail-fast: validation stops at the first invalid key.
         * ports[0] validates keys[0], ports[1] validates keys[1], and so on.
         * Each port bean must implement CompositeKeyValidator and be registered
         * in the Spring context under the name declared in the annotation.
         */
        for (int i = 0; i < ports.length; i++) {
            CompositeKeyValidator port = context.getBean(ports[i], CompositeKeyValidator.class);
            if (!port.exists(keys[i])) {
                throw new ValidationException("key.invalid",
                    Map.of("field", "key" + (i + 1),
                           "value", keys[i].toString()));
            }
        }

        /*
         * Step 6: All keys are valid — Spring AOP proceeds to the intercepted
         * method body automatically after this @Before advice returns normally.
         */
    }
}

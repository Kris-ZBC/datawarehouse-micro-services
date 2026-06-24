package local.sop.sopinfo.infrastructure.validation.compositekeys;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CompositeKeyValidationCondition implements Condition{
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String value = context.getEnvironment().getProperty("compositekey.validate.enabled");
        return "true".equalsIgnoreCase(value);
    }

}

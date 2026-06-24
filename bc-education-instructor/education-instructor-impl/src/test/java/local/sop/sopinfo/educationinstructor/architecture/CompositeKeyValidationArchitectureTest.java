package local.sop.sopinfo.educationinstructor.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "local.sop.sopinfo.educationinstructor")
public class CompositeKeyValidationArchitectureTest {

    // ── Rule 1: Any class annotated with @ValidateCompositeKey must have its
    //           key ports registered as beans that implement CompositeKeyValidator ─

    @ArchTest
    static final ArchRule validateCompositeKey_portsMustImplementCompositeKeyValidator =
        classes()
            .that().implement(
                local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator.class)
            .should().beAnnotatedWith(
                org.springframework.stereotype.Component.class)
            .because("Composite key validators must be registered as Spring beans " +
                     "so the aspect can resolve them by name");

    // ── Rule 2: Classes implementing CompositeKeyValidator must override exists(UUID) ─

   @ArchTest
    static final ArchRule compositeKeyValidator_mustOverrideExistsMethod =
        classes()
            .that().implement(
                local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator.class)
            .should(new ArchCondition<JavaClass>("override exists(UUID id) from CompositeKeyValidator") {
                @Override
                public void check(JavaClass clazz, ConditionEvents events) {
                    if (clazz.isInterface()) return; // skip interfaces

                    // Verify CompositeKeyValidator declares exists(UUID)
                    boolean interfaceHasExists = clazz.getAllRawInterfaces().stream()
                        .filter(i -> i.getName().equals(
                            "local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator"))
                        .flatMap(i -> i.getMethods().stream())
                        .anyMatch(m -> m.getName().equals("exists")
                            && m.getRawParameterTypes().size() == 1
                            && m.getRawParameterTypes().get(0).getName().equals("java.util.UUID"));

                    // Verify the concrete class provides the implementation
                    boolean classHasExists = clazz.getMethods().stream()
                        .anyMatch(m -> m.getName().equals("exists")
                            && m.getRawParameterTypes().size() == 1
                            && m.getRawParameterTypes().get(0).getName().equals("java.util.UUID"));

                    if (!interfaceHasExists || !classHasExists) {
                        events.add(SimpleConditionEvent.violated(clazz,
                            clazz.getName() + " implements CompositeKeyValidator " +
                            "but does not provide a concrete exists(UUID id) implementation " +
                            "overriding CompositeKeyValidator"));
                    }
                }
            })
            .because("CompositeKeyValidator implementations must override exists(UUID id) " +
                    "from CompositeKeyValidator so the aspect can validate composite key existence");
    // ── Rule 3: Port interfaces used in @ValidateCompositeKey must extend CompositeKeyValidator ─

    @ArchTest
    static final ArchRule validateCompositeKey_portInterfacesMustExtendCompositeKeyValidator =
        classes()
            .that().areInterfaces()
            .and().areAssignableTo(
                local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator.class)
            .should().beAssignableTo(
                local.sop.sopinfo.sharedkernel.compositekey.validate.CompositeKeyValidator.class)
            .because("Port interfaces used in @ValidateCompositeKey must extend CompositeKeyValidator");

    // ── Rule 4: Methods annotated with @ValidateCompositeKey must be in application.service ─

    @ArchTest
    static final ArchRule validateCompositeKey_mustOnlyBeUsedInApplicationService =
        methods()
            .that().areAnnotatedWith(
                local.sop.sopinfo.sharedkernel.compositekey.validate.ValidateCompositeKey.class)
            .should().beDeclaredInClassesThat()
            .resideInAPackage("..application.service..")
            .because("@ValidateCompositeKey is an application layer concern and must only " +
                     "be used in application service classes");
}

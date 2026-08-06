package local.sop.datawarehouse.messageperson.application.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;

@AnalyzeClasses(packages = {"local.sop.sopinfo.messageperson", "local.sop.common.libs.sharedkernel" },
    importOptions = {
        ImportOption.DoNotIncludeTests.class
    }
)

public class ApplicationLayerCleanTest {

    @ArchTest
    static final ArchRule api_must_not_depend_on_spring_or_jpa =
        noClasses().that().resideInAnyPackage("..application.api..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "javax.persistence..");
    
    @ArchTest
    static final ArchRule dto_must_not_depend_on_other_layers =    
        noClasses().that().resideInAnyPackage("..application.api..")
                .should().dependOnClassesThat().resideInAnyPackage("..domain..", 
                "..domain.service..", "..domain.model..", "..domain.ports.out..", "..config.security..", "..interfaceweb..",
                "..interfaceadapters.persistence.jpa..", "..security..")
                 .because("DTOs must be independent of all other layers in hexagonal architecture");
    
    @ArchTest
    static final ArchRule only_interfaces_directly_under_application_api =
        classes()
            .that().resideInAPackage("..application.api")   // exact package (no trailing "..")
            .should().beInterfaces()
            .andShould().bePublic()
            .because("application.api is our public SPI; it must consist of public interfaces only.");

     @ArchTest
    static final ArchRule dto_records_are_clean_except_validation =
    classes()
        .that().resideInAnyPackage("..application.api.dto..")
        .and().areRecords() // limit to records; drop this if DTOs can be classes too
        .should().onlyDependOnClassesThat(new DescribedPredicate<>("JDK, Jakarta Validation, or same DTO package") {
            @Override public boolean test(JavaClass c) {
                String pkg = c.getPackageName();
                return c.getName().startsWith("java.")         // JDK (includes java.lang.Record)
                    || pkg.startsWith("jakarta.validation")  
                    || pkg.contains(".application.api.dto.")
                    || pkg.contains(".sharedkernel.enums")   // shared kernel enums other DTOs  
                    || pkg.contains(".sharedkernel.compositekey.dtos"); 
            }
        })
        .because("DTO records must be independant of all other modules, except Jakarta Validation and shared enums.");
    
    @ArchTest
    static final ArchRule dtos_must_be_public =
        classes().that().resideInAnyPackage("..application.api.dto..")
            .should().bePublic()
            .because("DTOs are part of the public API and should be accessible to clients.");


    @ArchTest
    static final ArchRule dtos_must_end_with_response_or_result_or_request_or_command =
        classes()
            .that().resideInAnyPackage("..application.api.dto..")
            .should().haveSimpleNameEndingWith("Response")
            .orShould().haveSimpleNameEndingWith("Result")
            .orShould().haveSimpleNameEndingWith("Cmd")
            .orShould().haveSimpleNameEndingWith("Query")
            
            .because("DTOs must be clearly named to indicate their purpose");


    @ArchTest
    static final ArchRule every_field_in_command_and_query_request_dtos_must_have_validation =
        classes()
            .that().resideInAnyPackage("..application.api.dto..")
            .should(new ArchCondition<>("have every declared field / record component annotated with a jakarta.validation constraint (excluding *Response/*Result DTOs)") {
                @Override public void check(JavaClass clazz, ConditionEvents events) {
                    // Exclude response/result DTOs by naming and subpackage conventions
                    String simple = clazz.getSimpleName();
                    String pkg = clazz.getPackageName();
                    if (simple.endsWith("Response") || simple.endsWith("Result") || (simple.endsWith("Query") && simple.contains("Params"))
                            || pkg.endsWith(".response") || pkg.contains(".response.")
                            || pkg.endsWith(".result")   || pkg.contains(".result.")
                            || pkg.endsWith(".query")   && pkg.contains(".Params.")
                            ) {
                        return; // skip: response/result/Query params DTOs
                    }

                    for (JavaField field : clazz.getFields()) {
                        // Only fields declared on this class/record; ignore inherited
                        if (!field.getOwner().equals(clazz)) continue;
                        // Ignore constants/static helpers
                        if (field.getModifiers().contains(JavaModifier.STATIC)) continue;

                        boolean hasJakartaConstraint =
                            field.getAnnotations().stream()
                                .map(JavaAnnotation::getRawType)
                                .map(JavaClass::getPackageName)
                                .anyMatch(p -> p.startsWith("jakarta.validation"));

                        if (!hasJakartaConstraint) {
                            events.add(SimpleConditionEvent.violated(
                                field,
                                String.format("'%s.%s' lacks a jakarta.validation constraint",
                                                clazz.getName(), field.getName())
                            ));
                        }
                    }
                }
            })
            .because("Command/Request DTOs must validate all incoming data; Response/Result DTOs are excluded.");
    
}


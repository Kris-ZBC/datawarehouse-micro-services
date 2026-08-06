
package local.sop.datawarehouse.auditlog.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ApiArchitectureTest {

    private static final JavaClasses imported = new ClassFileImporter().importPackages("local.sop.sopinfo.auditlog");
    @Test
    void api_contract_should_be_framework_free() {

        // Nothing in the API module should pull in Spring/web/JPA/etc.
        noClasses()
                .that().resideInAPackage("..auditlog..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence..",
                        "jakarta.servlet..",
                        "javax.servlet..",
                        "org.hibernate.."
                )
                .because("audit-log-api is a contract module; it must stay framework-neutral.")
                .check(imported);
    }

    @Test
    void domain_inside_api_should_not_depend_on_application_api() {
        
        noClasses()
                .that().resideInAPackage("..auditlog.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..auditlog.application..")
                .because("Even if the API module contains domain enums/value objects, they must stay independent of the API layer.")
                .allowEmptyShould(true)
                .check(imported);
    }

    
}

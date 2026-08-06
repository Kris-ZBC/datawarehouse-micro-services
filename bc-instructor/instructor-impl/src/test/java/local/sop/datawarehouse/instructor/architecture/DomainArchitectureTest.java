package local.sop.datawarehouse.instructor.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

class DomainArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("local.sop.datawarehouse.instructor");

    @Test
    void domain_should_not_depend_on_application() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .check(classes);
    }

    @Test
    void domain_should_not_depend_on_interfaceweb() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..interfaceweb..")
                .check(classes);
    }

    @Test
    void domain_should_not_depend_on_persistence() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..interfaceadapters.persistence..")
                .check(classes);
    }
}
package local.sop.sopinfo.instructor.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

class ApplicationArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("local.sop.sopinfo.instructor");

    @Test
    void application_should_not_depend_on_interfaceweb() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..interfaceweb..")
                .check(classes);
    }

    @Test
    void application_should_not_depend_on_persistence() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..interfaceadapters.persistence..")
                .check(classes);
    }
}
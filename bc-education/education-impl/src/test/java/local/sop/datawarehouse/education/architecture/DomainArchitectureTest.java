package local.sop.datawarehouse.education.architecture;

import org.junit.jupiter.api.Test;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class DomainArchitectureTest {

    @Test
    public void domain_should_not_depend_on_other_packages() {
        var importedClasses = new ClassFileImporter().importPackages("local.sop.sopinfo.education");

        ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework",
                        "jakarta.persistence",
                        "javax.persistence",
                        "org.hibernate",
                        "..infrastructure..",
                        "..interfaceadapters..",
                        "..interfaceweb..",
                        "..application..")
                .allowEmptyShould(false) // CHANGE TO FALSE, WHEN THERE IS CLASSES IN THE DOMAIN LAYER
                .because("The domain layer should be independent of other layers.")
                .check(importedClasses);
    }
}

package local.sop.datawarehouse.educationline.architecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class DomainArchitectureTest {

    @Test
    public void domain_should_not_depend_on_other_packages() {
        var importedClasses = new ClassFileImporter().importPackages("local.sop.datawarehouse.educationline");

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
                "..application.."
            )
            .allowEmptyShould(false)
            .because("The domain layer should be independent of other layers.")
            .check(importedClasses);
    }
}

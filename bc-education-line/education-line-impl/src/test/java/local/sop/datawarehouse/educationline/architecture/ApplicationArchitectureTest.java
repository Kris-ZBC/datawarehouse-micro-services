package local.sop.datawarehouse.educationline.architecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class ApplicationArchitectureTest {

    @Test
    public void application_should_not_depend_on_adapters_or_infrastructure() {
        var importedClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("local.sop.datawarehouse.educationline");

        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..interfaceadapters..",
                "..interfaceweb..",
                "..infrastructure.."
            )
            .allowEmptyShould(false)
            .because("The application layer should not depend on adapters or infrastructure.")
            .check(importedClasses);
    }
}

package local.sop.datawarehouse.education.architecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class ApplicationArchitectureTest {

    @Test
    public void application_should_not_depend_on_adapters_or_infrastructure() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("local.sop.datawarehouse.education");

        ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..interfaceadapters..", "..infrastructure..", "..interfaceweb..")
                .allowEmptyShould(false) // CHANGE TO FALSE, WHEN THERE IS CLASSES IN THE APPLICATION LAYER
                .because("The application layer should be independent of other layers.")
                .check(importedClasses);

    }
}

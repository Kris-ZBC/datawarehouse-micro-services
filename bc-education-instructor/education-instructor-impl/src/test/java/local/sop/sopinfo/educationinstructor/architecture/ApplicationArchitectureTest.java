package local.sop.sopinfo.educationinstructor.architecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class ApplicationArchitectureTest {

    @Test
    public void application_should_not_depend_on_adapters() {
        var importedClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("local.sop.sopinfo.educationinstructor");

        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..interfaceadapters..",
                "..interfaceweb.."
            )
            .allowEmptyShould(false)
            .because("The application layer should not depend on adapters.")
            .check(importedClasses);
    }
}

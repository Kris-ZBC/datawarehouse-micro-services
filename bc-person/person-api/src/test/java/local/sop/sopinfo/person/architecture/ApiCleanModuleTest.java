package local.sop.sopinfo
.person.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "local.sop.sopinfo.person",
    importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class
    })
public class ApiCleanModuleTest {

    @ArchTest
    static final ArchRule api_must_be_clean_from_other_modules = 
    noClasses().that().
    resideInAPackage("..api..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "javax.persistence..");
}

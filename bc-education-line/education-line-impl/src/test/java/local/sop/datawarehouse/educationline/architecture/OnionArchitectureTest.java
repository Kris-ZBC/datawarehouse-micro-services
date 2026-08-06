package local.sop.datawarehouse.educationline.architecture;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(
    packages = "local.sop.datawarehouse.educationline",
    importOptions = { ImportOption.DoNotIncludeTests.class }
)
public class OnionArchitectureTest {

    @ArchTest
    void hexagonal_architecture(JavaClasses importedClasses) {
        onionArchitecture()
            .domainModels("..model..")
            .domainServices("..domain.service..", "..domain.ports.out..")
            .applicationServices("..application..", "..application.api..")
            .adapter("persistence", "..interfaceadapters.persistence..")
            .adapter("interfaceweb", "..interfaceweb..")
            .withOptionalLayers(false)
            .because("The educationline module must follow the Hexagonal/Onion Architecture pattern.")
            .check(importedClasses);
    }
}

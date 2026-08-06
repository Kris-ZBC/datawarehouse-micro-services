package local.sop.datawarehouse.education.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(
packages = "local.sop.sopinfo.education",
importOptions = ImportOption.DoNotIncludeTests.class)

public class OnionArchitectureTest {

    @ArchTest
    void hexagonal_architecture(JavaClasses importedClases) {
        onionArchitecture()
          .domainModels("..model..")
          .domainServices("..domain.service..", "..domain.ports.out..")
          .applicationServices("..application..", "..application.api..")
          .adapter("persistence", "..interfaceadapters.persistence..")
          .adapter("interfaceweb", "..interfaceweb..")
          .withOptionalLayers(false)
          .because("The education module must follow the Hexagonal/Onion Architecture pattern.")
          .check(importedClases);
    }
}

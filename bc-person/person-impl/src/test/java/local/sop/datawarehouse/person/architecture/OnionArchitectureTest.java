package local.sop.datawarehouse.person.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;

@AnalyzeClasses(packages = "local.sop.datawarehouse.person", importOptions = {
        ImportOption.DoNotIncludeTests.class, // <-- udelukker target/test-classes
})

public class OnionArchitectureTest {

    @ArchTest
    void hexagonal_architecture(JavaClasses importedClasses) {
        onionArchitecture()
            .domainModels("..model..")
            .applicationServices("..application..")
            .domainServices("..domain.service..", "..domain.ports.out..")
            .adapter("config", "..config.security..")
            .adapter("interfaceweb", "..interfaceweb..")
            .adapter("persistence", "..interfaceadapters.persistence.jpa..")
            .adapter("security", "..security..")
            .withOptionalLayers(true)
            .because("The person module must follow the Hexagonal Architecture pattern.")
            .check(importedClasses);
    }
}

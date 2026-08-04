package local.sop.sopinfo.educationinstructor.architecture;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(
    packages = {"local.sop.sopinfo.educationinstructor", "local.sop.common.libs.sharedkernel", "local.sop.common.libs.infrastructure"},
    importOptions = { ImportOption.DoNotIncludeTests.class }
)
public class OnionArchitectureTest {

    @ArchTest
    void hexagonal_architecture(JavaClasses importedClasses) {
        onionArchitecture()
            .domainModels("..domain.model", "..domain.model.valueobjects")
            .domainServices("..domain.service", "..domain.ports.out..")
            .applicationServices("..application.service", "..application.api..", "..application.api.dto")
            .adapter("persistence", "..interfaceadapters.persistence..")
            .adapter("interfaceweb", "..interfaceweb..")
            .adapter("infrastructure", "..infrastructure..")
            .withOptionalLayers(true)
            .because("The educationInstructor module must follow the Hexagonal Architecture pattern.")
            .check(importedClasses);
    }
}

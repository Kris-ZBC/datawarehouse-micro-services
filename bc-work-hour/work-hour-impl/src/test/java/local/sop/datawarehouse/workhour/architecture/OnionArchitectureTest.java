package local.sop.datawarehouse.workhour.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;



@AnalyzeClasses(packages = {"local.sop.sopinfo.workhour", "local.sop.common.libs.sharedkernel", "local.sop.common.libs.infrastructure"},
importOptions = {
    ImportOption.DoNotIncludeTests.class,   // <-- udelukker target/test-classes
})  
public class OnionArchitectureTest {


    @ArchTest
    void hexagonal_architecture(JavaClasses importedClasses) {
            onionArchitecture()
                .domainModels("..domain.model", "..domain.model.valueobjects")
                .applicationServices("..application.service", "..application.api..", "..application.api.dto")
                .domainServices("..domain.service", "..domain.ports.out..")
                .adapter("interfaceweb", "..interfaceweb..")
                .adapter("persistence", "..interfaceadapters.persistence.jpa..")
                .adapter("infrastructure.security", "..infrastructure.security..")
                //.withOptionalLayers(true) // uncomment for empty packages
                .because("The Workhour BC must follow the Hexagonal Architecture pattern.")
                .check(importedClasses);
    }
}


package local.sop.datawarehouse.instructor.architecture;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

class OnionArchitectureTest {

    @Test
    void should_follow_onion_architecture() {
        var classes = new ClassFileImporter()
                .importPackages("local.sop.datawarehouse.instructor");

        onionArchitecture()
                .withOptionalLayers(true)
                .domainModels("local.sop.datawarehouse.instructor.domain.model..")
                .domainServices(
                        "local.sop.datawarehouse.instructor.domain.service..",
                        "local.sop.datawarehouse.instructor.domain.ports.."
                )
                .applicationServices("local.sop.datawarehouse.instructor.application.service..")
                .adapter("persistence", "local.sop.datawarehouse.instructor.interfaceadapters.persistence..")
                .adapter("web", "local.sop.datawarehouse.instructor.interfaceweb..")
                .check(classes);
    }
}
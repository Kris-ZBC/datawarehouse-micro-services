package local.sop.sopinfo.instructor.architecture;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

class OnionArchitectureTest {

    @Test
    void should_follow_onion_architecture() {
        var classes = new ClassFileImporter()
                .importPackages("local.sop.sopinfo.instructor");

        onionArchitecture()
                .withOptionalLayers(true)
                .domainModels("local.sop.sopinfo.instructor.domain.model..")
                .domainServices(
                        "local.sop.sopinfo.instructor.domain.service..",
                        "local.sop.sopinfo.instructor.domain.ports.."
                )
                .applicationServices("local.sop.sopinfo.instructor.application.service..")
                .adapter("persistence", "local.sop.sopinfo.instructor.interfaceadapters.persistence..")
                .adapter("web", "local.sop.sopinfo.instructor.interfaceweb..")
                .check(classes);
    }
}
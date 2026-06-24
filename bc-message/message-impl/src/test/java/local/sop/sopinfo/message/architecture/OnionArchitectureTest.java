package local.sop.sopinfo.message.architecture;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

class OnionArchitectureTest {

    @Test
    @DisplayName("message module should follow onion architecture")
    void messageModuleShouldFollowOnionArchitecture() {
        var importedClasses = new ClassFileImporter()
            .importPackages("local.sop.sopinfo.message");

        onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.service..", "..domain.ports.out..")
            .applicationServices("..application..", "..application.mapper..")
            .adapter("interfaceweb", "..interfaceweb..")
            .adapter("persistence", "..interfaceadapters.persistence.jpa..")
            .withOptionalLayers(true)
            .check(importedClasses);
    }
}
package local.sop.datawarehouse.consent.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(
  packages = {"local.sop.datawarehouse.consent", "local.sop.common.libs.sharedkernel", "local.sop.common.libs.infrastructure"},
  importOptions = { ImportOption.DoNotIncludeTests.class }
)
public class OnionArchitectureTest {

  @ArchTest
  void hexagonal_architecture(JavaClasses importedClasses) {
        // CHANGED: bootstrap/seed data (interfaceadapters.bootstrap) is
        // declared as its own adapter here. A CommandLineRunner
        // reaching in and calling a port at startup is a driving
        // adapter, the same category as a controller calling a
        // service — it's not "outside the architecture," it's a
        // fourth legitimate entry point. As a declared adapter it may
        // depend on the domain/application layers (which is all this
        // seeder does — ConsentStatementRepositoryPort and the domain
        // ConsentStatement model) but, like every other adapter, may
        // NOT depend on config/interfaceweb/persistence.
        onionArchitecture()
            .domainModels("..domain.model", "..domain.model.valueobjects")
            .domainServices("..domain.service..", "..domain.ports.out..")
            .applicationServices("..application.service", "..application.api..", "..application.api.dto")
            .adapter("config", "..config..")
            .adapter("interfaceweb", "..interfaceweb..")
            .adapter("persistence", "..interfaceadapters.persistence.jpa..")
            .adapter("bootstrap", "..interfaceadapters.bootstrap..")
        .because("The consent module must follow the Hexagonal Architecture pattern. " +
                 "bootstrap is a driving adapter (CommandLineRunner startup seeding) — " +
                 "may depend on domain/application layers, may not depend on the other adapters.")
        .check(importedClasses);
  }
}

package local.sop.datawarehouse.auditlog.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(
  packages = {"local.sop.datawarehouse.auditlog", "local.sop.common.libs.sharedkernel", "local.sop.common.libs.infrastructure"},
  importOptions = { ImportOption.DoNotIncludeTests.class }
)
public class OnionArchitectureTest {

  @ArchTest
  void hexagonal_architecture(JavaClasses importedClasses) {
		onionArchitecture()
			.domainModels("..domain.model", "..domain.model.valueobjects")
			.domainServices("..domain.service", "..domain.ports.out..")
			.applicationServices("..application.service", "..application.api..", "..application.api.dto")
			.adapter("config", "..config..")
			.adapter("interfaceweb", "..interfaceweb..")
			.adapter("persistence", "..interfaceadapters.persistence.jpa..")
		.because("The auditLog module must follow the Hexagonal Architecture pattern.")
		.check(importedClasses);
  }
}

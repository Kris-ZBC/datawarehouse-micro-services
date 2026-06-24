
package local.sop.sopinfo.auditlog.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class ApplicationArchitectureTest {

  @Test
  void application_must_not_depend_on_adapters() {
  var classes = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .importPackages("local.sop.sopinfo.auditlog");

    ArchRuleDefinition.noClasses().that()
      .resideInAnyPackage("..application..")
      .should().dependOnClassesThat()
      .resideInAnyPackage("..infrastructure..", "..interfaceadapters..", "..interfaceweb..")
      .check(classes);
  }
}

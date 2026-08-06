package local.sop.datawarehouse.sopeducation.architecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class ApplicationArchitectureTest {

  @Test
  void application_must_not_depend_on_adapters() {
    var classes = new ClassFileImporter().importPackages("local.sop.datawarehouse.sopeducation");
    ArchRuleDefinition.noClasses().that()
      .resideInAnyPackage("..application..")
      .should().dependOnClassesThat()
      .resideInAnyPackage("..interfaceadapters..", "..interfaceweb..")
      .check(classes); 
  }
}


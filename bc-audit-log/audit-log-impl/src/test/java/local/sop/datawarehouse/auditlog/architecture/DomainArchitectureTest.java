
package local.sop.datawarehouse.auditlog.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class DomainArchitectureTest {

  @Test
  void domain_must_be_framework_free() {
    var classes = new ClassFileImporter().importPackages("local.sop.sopinfo.auditlog");

    ArchRuleDefinition.noClasses().that()
      .resideInAnyPackage("..domain..") 
      .should().dependOnClassesThat()
      .resideInAnyPackage(
        "org.springframework..",
        "jakarta.persistence..",
        "javax.persistence..",
        "org.hibernate..",
        "..infrastructure..",
        "..interfaceadapters..",
        "..interfaceweb..",
        "..application.."
      )
      .check(classes);
  }
}

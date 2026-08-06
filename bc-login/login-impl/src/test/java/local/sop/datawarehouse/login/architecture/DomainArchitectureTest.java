package local.sop.datawarehouse.login.architecture;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class DomainArchitectureTest {
	
	@Test
	void domain_must_be_framework_free() {
		JavaClasses classes = new ClassFileImporter().importPackages("local.sop.sopinfo.login");

		ArchRuleDefinition.noClasses().that()
			.resideInAnyPackage("..domain.model", "..domain.ports.out")
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

	@Test
	void domain_must_be_framework_free_except_config() {
		JavaClasses classes = new ClassFileImporter().importPackages("local.sop.sopinfo.login");

		ArchRuleDefinition.noClasses().that()
			.resideInAnyPackage("..domain.service")
				.and().areNotAnnotatedWith("org.springframework.stereotype.Component")
				.and().areNotAnnotatedWith("org.springframework.context.annotation.Bean")
				.and().areNotAnnotatedWith("org.springframework.context.annotation.Configuration")
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

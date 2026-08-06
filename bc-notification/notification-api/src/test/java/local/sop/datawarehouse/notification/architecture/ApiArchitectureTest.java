package local.sop.datawarehouse.notification.architecture;

// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// import org.junit.jupiter.api.Test;

// import com.tngtech.archunit.core.domain.JavaClasses;
// import com.tngtech.archunit.core.importer.ClassFileImporter;

// public class ApiArchitectureTest {

// 	private static final JavaClasses imported = new ClassFileImporter().importPackages("local.sop.sopinfo.notification");

// 	@Test
// 	void api_contract_should_be_framework_free() {

// 		noClasses()
// 				.that().resideInAPackage("..notification..")
// 				.should().dependOnClassesThat().resideInAnyPackage(
// 					"org.springframework..",
// 					"jakarta.persistence..",
// 					"javax.persistence..",
// 					"jakarta.servlet..",
// 					"javax.servlet..",
// 					"org.hibernate.."
// 				)
// 				.because("notification-api is a contract module; it must stay framework-neutral.")
// 				.check(imported);
// 	}

// }

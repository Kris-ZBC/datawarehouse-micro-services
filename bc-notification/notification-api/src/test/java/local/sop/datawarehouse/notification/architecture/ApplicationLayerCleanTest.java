package local.sop.datawarehouse.notification.architecture;

// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// import com.tngtech.archunit.base.DescribedPredicate;
// import com.tngtech.archunit.core.domain.JavaAnnotation;
// import com.tngtech.archunit.core.domain.JavaClass;
// import com.tngtech.archunit.core.domain.JavaField;
// import com.tngtech.archunit.core.domain.JavaModifier;
// import com.tngtech.archunit.core.importer.ImportOption;
// import com.tngtech.archunit.junit.AnalyzeClasses;
// import com.tngtech.archunit.junit.ArchTest;
// import com.tngtech.archunit.lang.ArchCondition;
// import com.tngtech.archunit.lang.ArchRule;
// import com.tngtech.archunit.lang.ConditionEvents;
// import com.tngtech.archunit.lang.SimpleConditionEvent;

// @AnalyzeClasses(packages = "local.sop.sopinfo.notification",
// 	importOptions = {
// 		ImportOption.DoNotIncludeTests.class,
// 	})
// public class ApplicationLayerCleanTest {
// 	@ArchTest
// 	static final ArchRule api_must_not_depend_on_spring_or_jpa =
// 		noClasses().that().resideInAnyPackage("..application.api..")
// 				.should().dependOnClassesThat().resideInAnyPackage(
// 					"org.springframework..", "jakarta.persistence..", "javax.persistence..");

// 	 @ArchTest
// 	static final ArchRule dto_must_not_depend_on_each_other =    
// 		noClasses().that().resideInAnyPackage("..application.api.dto..")
// 				.should().dependOnClassesThat().resideInAnyPackage("..application.api.dto..")
// 				.because("DTOs must be independent of each other to avoid cyclic dependencies");    

// 	@ArchTest
// 	static final ArchRule dto_must_not_depend_on_other_layers =    
// 		noClasses().that().resideInAnyPackage("..application.api..")
// 				.should().dependOnClassesThat().resideInAnyPackage("..domain..", 
// 				"..domain.service..", "..domain.model..", "..domain.ports.out..", "..config.security..", "..interfaceweb..",
// 				"..interfaceadapters.persistence.jpa..", "..security..")
// 				 .because("DTOs must be independent of all other layers in hexagonal architecture");
	
// 	@ArchTest
// 	static final ArchRule only_interfaces_directly_under_application_api =
// 		classes()
// 			.that().resideInAPackage("..application.api")
// 			.should().beInterfaces()
// 			.andShould().bePublic()
// 			.because("application.api is our public SPI; it must consist of public interfaces only.");
	
		
// 	@ArchTest
// 	static final ArchRule dto_records_are_clean_except_validation =
// 	classes()
// 		.that().resideInAnyPackage("..application.api.dto..")
// 		.and().areRecords()
// 		.should().onlyDependOnClassesThat(new DescribedPredicate<>("JDK, Jakarta Validation, or same DTO package") {
// 			@Override public boolean test(JavaClass c) {
// 				String pkg = c.getPackageName();
// 				return c.getName().startsWith("java.")
// 					|| pkg.startsWith("jakarta.validation")  
// 					|| pkg.contains("java.util")
// 					|| pkg.contains(".application.api.dto.")
// 					|| pkg.contains(".sharedkernel."); 
// 			}
// 		})
// 		.because("DTO records must be independent of all other modules, except Jakarta Validation.");
				 

// 	@ArchTest
// 	static final ArchRule dto_must_not_depend_on_other_modules =    
// 		noClasses().that().resideInAnyPackage("..application.api.dto..")
// 				.should().dependOnClassesThat().resideInAnyPackage("..application", "..config..", "..domain..", 
// 				"..interfaceadapters..", "..interfaceweb..", "..security..")
// 				 .because("DTOs must be independent of other modules to ensure loose coupling");


// 	@ArchTest
// 	static final ArchRule dtos_must_end_with_response_or_result_or_request_or_command =
// 		classes()
// 			.that().resideInAnyPackage("..application.api.dto..")
// 			.should().haveSimpleNameEndingWith("Response")
// 			.orShould().haveSimpleNameEndingWith("Result")
// 			.orShould().haveSimpleNameEndingWith("Cmd")
// 			.orShould().haveSimpleNameEndingWith("Query")
			
// 			.because("DTOs must be clearly named to indicate their purpose");

//   @ArchTest
//   static final ArchRule every_field_in_command_and_query_request_dtos_must_have_validation =
// 	  classes()
// 		  .that().resideInAnyPackage("..application.api.dto..")
// 		  .should(new ArchCondition<>("have every declared field / record component annotated with a jakarta.validation constraint (excluding *Response/*Result DTOs)") {
// 			  @Override public void check(JavaClass clazz, ConditionEvents events) {
// 				  // Exclude response/result DTOs by naming and subpackage conventions
// 				  String simple = clazz.getSimpleName();
// 				  String pkg = clazz.getPackageName();
// 				  if (simple.endsWith("Response") || simple.endsWith("Result") || (simple.endsWith("Query") && simple.contains("Params"))
// 						  || pkg.endsWith(".response") || pkg.contains(".response.")
// 						  || pkg.endsWith(".result")   || pkg.contains(".result.")
// 						  || pkg.endsWith(".query")   && pkg.contains(".Params.")
// 						) {
// 					  return;
// 				  }

// 				  for (JavaField field : clazz.getFields()) {
// 					  // Only fields declared on this class/record; ignore inherited
// 					  if (!field.getOwner().equals(clazz)) continue;
// 					  // Ignore constants/static helpers
// 					  if (field.getModifiers().contains(JavaModifier.STATIC)) continue;

// 					  boolean hasJakartaConstraint =
// 						  field.getAnnotations().stream()
// 							  .map(JavaAnnotation::getRawType)
// 							  .map(JavaClass::getPackageName)
// 							  .anyMatch(p -> p.startsWith("jakarta.validation"));

// 					  if (!hasJakartaConstraint) {
// 						  events.add(SimpleConditionEvent.violated(
// 							  field,
// 							  String.format("'%s.%s' lacks a jakarta.validation constraint",
// 											clazz.getName(), field.getName())
// 						  ));
// 					  }
// 				  }
// 			  }
// 		  })
// 		  .because("Command/Request DTOs must validate all incoming data; Response/Result DTOs are excluded.");
// }

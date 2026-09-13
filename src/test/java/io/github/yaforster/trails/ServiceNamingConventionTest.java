package io.github.yaforster.trails;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ServiceNamingConventionTest {

	private final JavaClasses classes = new ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages("io.github.yaforster.trails");

	@Test
	void springServiceClassesShouldEndWithService() {
		classes().that()
			.areAnnotatedWith(Service.class)
			.should()
			.haveSimpleNameEndingWith("ServiceImpl")
			.orShould()
			.haveSimpleNameEndingWith("FactoryImpl")
			.allowEmptyShould(true)
			.check(classes);
	}

}

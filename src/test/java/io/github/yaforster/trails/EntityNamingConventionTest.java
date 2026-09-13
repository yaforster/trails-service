package io.github.yaforster.trails;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class EntityNamingConventionTest {

	private final JavaClasses classes = new ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages("io.github.yaforster.trails");

	@Test
	void entityClassesShouldEndWithEntity() {
		classes().that()
			.areAnnotatedWith(Entity.class)
			.should()
			.haveSimpleNameEndingWith("Entity")
			.allowEmptyShould(true)
			.check(classes);
	}

}

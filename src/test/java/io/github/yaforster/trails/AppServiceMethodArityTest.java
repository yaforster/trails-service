package io.github.yaforster.trails;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AppServiceMethodArityTest {

	private final JavaClasses classes = new ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages("io.github.yaforster.trails.app.services");

	@Test
	void publicServiceInstanceMethods_shouldHaveAtMostOneArgument() {
		List<String> invalidMethods = classes.stream()
			.filter(JavaClass::isInterface)
			.flatMap(service -> service.getMethods()
				.stream()
				.filter(method -> method.getOwner().equals(service))
				.filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
				.filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
				.filter(method -> method.getRawParameterTypes().size() > 1)
				.map(AppServiceMethodArityTest::describe))
			.toList();

		assertThat(invalidMethods).isEmpty();
	}

	private static String describe(JavaMethod method) {
		return method.getFullName();
	}

}

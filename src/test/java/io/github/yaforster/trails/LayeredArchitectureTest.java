package io.github.yaforster.trails;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.List;
import java.util.stream.Stream;

public class LayeredArchitectureTest {

	private final JavaClasses classes = new ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackagesOf(TrailsService.class);

	@Test
	public void layer_dependencies_are_respected() {
		layeredArchitecture().consideringOnlyDependenciesInLayers()
			.layer("Bootstrap")
			.definedBy("io.github.yaforster.trails")
			.layer("Application")
			.definedBy("..app..")
			.layer("Core")
			.definedBy("..core..")
			.layer("Adapter")
			.definedBy("..adapter..")
			.whereLayer("Bootstrap")
			.mayNotBeAccessedByAnyLayer()
			.whereLayer("Core")
			.mayOnlyBeAccessedByLayers("Application", "Adapter")
			.whereLayer("Application")
			.mayOnlyBeAccessedByLayers("Adapter")
			.whereLayer("Adapter")
			.mayNotBeAccessedByAnyLayer()
			.check(classes);
	}

	@Test
	public void generated_api_dtos_do_not_leak_beyond_api_adapter() {
		noClasses().that()
			.resideOutsideOfPackage("..adapter.api..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage("..adapter.api.rest.model..", "..adapter.api.asyncapi.model..")
			.check(classes);
	}

	@Test
	public void adapter_features_are_isolated() {
		List<String> adapterFeaturePackages = List.of("..adapter.capability..", "..adapter.db..", "..adapter.print..",
				"..adapter.runtime..", "..adapter.testdata..", "..adapter.test.execution..",
				"..adapter.test.webdriver..");

		noClasses().that()
			.resideInAPackage("..adapter.api..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage(adapterFeaturePackages.toArray(String[]::new))
			.check(classes);
		for (String adapterFeaturePackage : adapterFeaturePackages) {
			String[] forbiddenAdapterPackages = Stream
				.concat(Stream.of("..adapter.api.."),
						adapterFeaturePackages.stream().filter(candidate -> !candidate.equals(adapterFeaturePackage)))
				.toArray(String[]::new);
			noClasses().that()
				.resideInAPackage(adapterFeaturePackage)
				.should()
				.dependOnClassesThat()
				.resideInAnyPackage(forbiddenAdapterPackages)
				.allowEmptyShould(true)
				.check(classes);
		}
	}

	@Test
	public void database_adapter_internals_do_not_leak_to_other_packages() {
		noClasses().that()
			.resideOutsideOfPackage("..adapter.db..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage("..adapter.db..")
			.check(classes);
	}

	@Test
	public void database_features_are_isolated() {
		List<String> databaseFeaturePackages = List.of("..adapter.db.application..", "..adapter.db.artifact..",
				"..adapter.db.deployment..", "..adapter.db.element.screenshot..", "..adapter.db.result.action..",
				"..adapter.db.result.run..", "..adapter.db.result.testset..", "..adapter.db.result.testpath..",
				"..adapter.db.stage..", "..adapter.db.testdata..", "..adapter.db.testplan..", "..adapter.db.user..");

		for (String databaseFeaturePackage : databaseFeaturePackages) {
			String[] forbiddenDatabaseFeaturePackages = databaseFeaturePackages.stream()
				.filter(candidate -> !candidate.equals(databaseFeaturePackage))
				.toArray(String[]::new);
			noClasses().that()
				.resideInAPackage(databaseFeaturePackage)
				.should()
				.dependOnClassesThat()
				.resideInAnyPackage(forbiddenDatabaseFeaturePackages)
				.allowEmptyShould(true)
				.check(classes);
		}
	}

	@Test
	public void core_and_application_only_depend_on_approved_programming_utilities() {
		classes().that()
			.resideInAnyPackage("..core..", "..app..")
			.should()
			.onlyDependOnClassesThat()
			.resideInAnyPackage("io.github.yaforster.trails.core..", "io.github.yaforster.trails.app..", "java..",
					"javax..", "lombok..", "com.google.common..", "org.apache.commons..", "org.mapstruct..")
			.check(classes);
	}

}

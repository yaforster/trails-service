package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.application.ApplicationController;
import io.github.yaforster.trails.adapter.api.rest.element.ElementController;
import io.github.yaforster.trails.adapter.api.rest.stage.StageController;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class RetirementControllerAuthorizationTest {

	private static final String MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED = "@resourceAuthorization.canManageResources(authentication)";

	@Test
	void deleteApplication_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = ApplicationController.class.getMethod("deleteApplication", Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void restoreApplication_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = ApplicationController.class.getMethod("restoreApplication", Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void deleteStage_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled() throws NoSuchMethodException {
		Method method = StageController.class.getMethod("deleteStage", Long.class, Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void restoreStage_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled() throws NoSuchMethodException {
		Method method = StageController.class.getMethod("restoreStage", Long.class, Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void deleteElement_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled() throws NoSuchMethodException {
		Method method = ElementController.class.getMethod("deleteElement", Long.class, Long.class, Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void restoreElement_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = ElementController.class.getMethod("restoreElement", Long.class, Long.class, Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void promoteElement_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = ElementController.class.getMethod("promoteElement", Long.class, Long.class, Long.class,
				Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void deleteTestPlan_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = TestPlanController.class.getMethod("deleteTestPlan", Long.class, Long.class, Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void restoreTestPlan_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = TestPlanController.class.getMethod("restoreTestPlan", Long.class, Long.class, Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

	@Test
	void promoteTestPlan_shouldRequireConfiguredTestManagerOrAdminRole_whenOauthIsEnabled()
			throws NoSuchMethodException {
		Method method = TestPlanController.class.getMethod("promoteTestPlan", Long.class, Long.class, Long.class,
				Long.class);

		assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(MANAGER_OR_ADMIN_WHEN_OAUTH_ENABLED);
	}

}

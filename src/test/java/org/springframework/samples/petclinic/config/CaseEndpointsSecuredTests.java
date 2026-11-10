package org.springframework.samples.petclinic.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Every endpoint addressing a case by id must check access to that case. Guards against
 * endpoints like {@code /reports/{caseId}} being added without {@code @PreAuthorize}.
 */
class CaseEndpointsSecuredTests {

	private static final String BASE_PACKAGE = "org.springframework.samples.petclinic";

	@Test
	void everyCaseEndpointHasPreAuthorize() throws Exception {
		List<String> caseEndpoints = new ArrayList<>();
		List<String> unsecured = new ArrayList<>();

		for (Class<?> controller : controllers()) {
			RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
			String prefix = classMapping == null || classMapping.path().length == 0 ? "" : classMapping.path()[0];
			for (Method method : controller.getDeclaredMethods()) {
				RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
				if (mapping == null) {
					continue;
				}
				for (String path : mapping.path()) {
					String fullPath = prefix + path;
					if (!fullPath.contains("{caseId}")) {
						continue;
					}
					String endpoint = controller.getSimpleName() + "." + method.getName() + " " + fullPath;
					caseEndpoints.add(endpoint);
					if (!method.isAnnotationPresent(PreAuthorize.class)) {
						unsecured.add(endpoint);
					}
				}
			}
		}

		assertThat(caseEndpoints).as("sanity check: case endpoints were found").isNotEmpty();
		assertThat(caseEndpoints).as("report endpoint is covered").anyMatch(e -> e.contains("/reports/{caseId}"));
		assertThat(unsecured).as("case endpoints without @PreAuthorize").isEmpty();
	}

	private static List<Class<?>> controllers() throws ClassNotFoundException {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
		List<Class<?>> result = new ArrayList<>();
		for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
			result.add(
					ClassUtils.forName(candidate.getBeanClassName(), CaseEndpointsSecuredTests.class.getClassLoader()));
		}
		return result;
	}

}

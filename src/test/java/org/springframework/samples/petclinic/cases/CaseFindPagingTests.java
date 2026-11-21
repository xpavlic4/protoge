package org.springframework.samples.petclinic.cases;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.system.CustomUserPrincipal;
import org.springframework.samples.petclinic.system.User;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;

/**
 * The page request parameter is user controlled; zero or negative values must not reach
 * {@code PageRequest.of} as a negative index.
 */
class CaseFindPagingTests {

	private final CaseRepository cases = mock(CaseRepository.class);

	private final CaseController controller = new CaseController(cases, null, null, null, null, null);

	@Test
	void nonPositivePageIsClampedToFirstPage() {
		when(cases.findByNameStartingWithAndMember(anyString(), anyLong(), any(Pageable.class)))
			.thenReturn(new PageImpl<>(java.util.List.of()));
		User user = new User();
		user.setId(1L);
		CustomUserPrincipal principal = new CustomUserPrincipal(user);
		Case aCase = new Case();

		for (int page : new int[] { 0, -1, Integer.MIN_VALUE }) {
			assertThatNoException().isThrownBy(() -> controller.processFindForm(page, aCase,
					new BeanPropertyBindingResult(aCase, "case"), new ExtendedModelMap(), principal));
		}

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(cases, org.mockito.Mockito.times(3)).findByNameStartingWithAndMember(anyString(), anyLong(),
				captor.capture());
		captor.getAllValues().forEach(p -> org.assertj.core.api.Assertions.assertThat(p.getPageNumber()).isZero());
	}

}

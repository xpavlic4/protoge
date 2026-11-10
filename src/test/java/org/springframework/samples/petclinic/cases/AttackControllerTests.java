package org.springframework.samples.petclinic.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * An attack may only target an argument of the case in the URL; the ownership check only
 * covers the case id.
 */
class AttackControllerTests {

	private final CaseRepository cases = mock(CaseRepository.class);

	private final ArgumentRepository arguments = mock(ArgumentRepository.class);

	private final AttackController controller = new AttackController(cases, arguments);

	@Test
	void argumentOfTheSameCaseIsFound() {
		Argument argument = argument(10, 1);

		assertThat(controller.findArgumentInCase(1, 10)).isSameAs(argument);
	}

	@Test
	void argumentOfAnotherCaseIsRejected() {
		argument(20, 2);

		assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(() -> controller.findArgumentInCase(1, 20))
			.satisfies(e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void unknownArgumentIsRejected() {
		when(arguments.findById(99)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(() -> controller.findArgumentInCase(1, 99));
	}

	private Argument argument(int id, int caseId) {
		Case aCase = new Case();
		aCase.setId(caseId);
		Argument argument = new Argument();
		argument.setId(id);
		argument.setCase(aCase);
		when(arguments.findById(id)).thenReturn(Optional.of(argument));
		return argument;
	}

}

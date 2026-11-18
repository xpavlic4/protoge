/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.cases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.cases.Argument;
import org.springframework.samples.petclinic.cases.ArgumentType;
import org.springframework.samples.petclinic.cases.ArgumentValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link ArgumentValidator}
 *
 * @author Wick Dynex
 */
@ExtendWith(MockitoExtension.class)
@DisabledInNativeImage
class ArgumentValidatorTest {

	private ArgumentValidator argumentValidator;

	private Argument argument;

	private ArgumentType argumentType;

	private Errors errors;

	private static final String argumentPredicate = "predicate 1";

	private static final String petTypeName = "Dog";

	@BeforeEach
	void setUp() {
		argumentValidator = new ArgumentValidator();
		argument = new Argument();
		argumentType = new ArgumentType();
		errors = new MapBindingResult(new HashMap<>(), "pet");
	}

	@Test
	void validate() {
		argumentType.setName(petTypeName);
		argument.setPredicate(argumentPredicate);
		argument.setPremise("premise 1");
		argument.setType(argumentType);

		argumentValidator.validate(argument, errors);

		assertFalse(errors.hasErrors());
	}

	@Nested
	class ValidateHasErrors {

		@Test
		void validateWithInvalidPremise() {
			argumentType.setName(petTypeName);
			argument.setPredicate(argumentPredicate);
			argument.setType(argumentType);

			argumentValidator.validate(argument, errors);

			assertTrue(errors.hasFieldErrors("premise"));
		}

	}

}

/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.List;
import java.util.Optional;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>Case</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Wick Dynex
 */
public interface CaseRepository extends JpaRepository<Case, Integer> {

	/**
	 * Retrieve all {@link ArgumentType}s from the data store.
	 * @return a Collection of {@link ArgumentType}s.
	 */
	@Query("FROM ArgumentType ptype ORDER BY ptype.name")
	@Transactional(readOnly = true)
	List<ArgumentType> findArgumentTypes();

	@Query("FROM SourceType ptype ORDER BY ptype.name")
	@Transactional(readOnly = true)
	List<SourceType> findASourceTypes();

	/**
	 * Retrieve {@link Case}s from the data store by last name, returning all owners whose
	 * last name <i>starts</i> with the given name.
	 * @param name Value to search for
	 * @return a Collection of matching {@link Case}s (or an empty Collection if none
	 * found)
	 */
	Page<Case> findByNameStartingWith(String name, Pageable pageable);

	Page<Case> findByNameStartingWithAndUserId(String name, Long userId, Pageable pageable);

	/** Cases in which the user holds any role (judge, plaintiff or defendant). */
	@Query("SELECT c FROM Case c WHERE c.name LIKE CONCAT(:name, '%') AND EXISTS "
			+ "(SELECT r FROM CaseRole r WHERE r.aCase = c AND r.user.id = :userId)")
	Page<Case> findByNameStartingWithAndMember(String name, Long userId, Pageable pageable);

	/**
	 * Retrieve an {@link Case} from the data store by id.
	 * <p>
	 * This method returns an {@link Optional} containing the {@link Case} if found. If no
	 * {@link Case} is found with the provided id, it will return an empty
	 * {@link Optional}.
	 * </p>
	 * @param id the id to search for
	 * @return an {@link Optional} containing the {@link Case} if found, or an empty
	 * {@link Optional} if not found.
	 * @throws IllegalArgumentException if the id is null (assuming null is not a valid
	 * input for id)
	 */
	Optional<Case> findById(@Nonnull Integer id);

	/**
	 * Returns all the cases from data store
	 **/
	Page<Case> findAll(Pageable pageable);

}

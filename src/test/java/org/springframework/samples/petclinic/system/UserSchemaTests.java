package org.springframework.samples.petclinic.system;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;

/**
 * Stores and loads a user in a fresh Postgres database created from
 * {@code db/postgres/schema.sql}, so the schema must have every column the {@link User}
 * entity maps.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DisabledInNativeImage
class UserSchemaTests {

	@Container
	static final GenericContainer<?> postgres = new GenericContainer<>("postgres:18.3")
		.withEnv("POSTGRES_USER", "petclinic")
		.withEnv("POSTGRES_PASSWORD", "petclinic")
		.withEnv("POSTGRES_DB", "petclinic")
		.withExposedPorts(5432)
		.waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2));

	@DynamicPropertySource
	static void database(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url",
				() -> "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/petclinic");
		registry.add("spring.datasource.username", () -> "petclinic");
		registry.add("spring.datasource.password", () -> "petclinic");
		registry.add("spring.sql.init.mode", () -> "always");
		registry.add("spring.sql.init.schema-locations", () -> "classpath:db/postgres/schema.sql");
		registry.add("spring.sql.init.data-locations", () -> "");
	}

	@Autowired
	private UserRepository users;

	@Autowired
	private EntityManager entityManager;

	@Test
	void userIsStoredWithAllMappedColumns() {
		User user = new User();
		user.setName("Alice");
		user.setEmail("alice@example.com");
		user.setLanguage("cs");
		user.setAdmin(true);
		Long id = users.saveAndFlush(user).getId();
		entityManager.clear();

		User loaded = users.findById(id).orElseThrow();
		assertThat(loaded.getLanguage()).isEqualTo("cs");
		assertThat(loaded.isAdmin()).isTrue();
	}

}

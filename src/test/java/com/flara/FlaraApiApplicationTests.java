package com.flara;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test for the Spring application context.
 *
 * <p>Less trivial than it looks. {@code @SpringBootTest} starts the entire
 * context, so this fails if any bean cannot be constructed, any
 * {@code @Value} placeholder cannot be resolved, or any entity cannot be
 * mapped. A good share of configuration mistakes are caught here before they
 * ever reach a deployment.
 *
 * <p>It verifies no behaviour, though. Nothing in this project yet asserts that
 * one user cannot read another user's records, which is the most important
 * rule in the codebase and the first thing worth covering with real tests.
 *
 * <p>Runs against in-memory H2, configured in
 * {@code src/test/resources/application.properties}, so no database
 * credentials are needed to run the suite.
 */
@SpringBootTest
class FlaraApiApplicationTests {

	/** Fails if the application context cannot be built. */
	@Test
	void contextLoads() {
	}

}

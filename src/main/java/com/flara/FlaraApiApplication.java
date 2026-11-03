package com.flara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * <p>{@code @SpringBootApplication} is a composite of three annotations:
 * <ul>
 *   <li>{@code @Configuration}, marking this class as a source of bean
 *       definitions;</li>
 *   <li>{@code @EnableAutoConfiguration}, which configures the application from
 *       what is on the classpath, so the PostgreSQL driver produces a
 *       datasource, Spring Web MVC starts an embedded server, and Spring
 *       Security builds a filter chain, with no XML anywhere;</li>
 *   <li>{@code @ComponentScan}, which registers every annotated bean found in
 *       this class's package and below.</li>
 * </ul>
 *
 * <p>That last one is why this class lives in {@code com.flara} rather than in a
 * sub-package. Scanning starts here and works downward, which is how
 * {@code controller}, {@code service}, {@code repository}, {@code security} and
 * {@code exception} are all discovered without being registered by hand. Moving
 * this file into a sub-package would silently hide the rest of the application
 * from the scan.
 */
@SpringBootApplication
public class FlaraApiApplication {

	/**
	 * Boots the Spring context and starts the embedded web server.
	 *
	 * @param args command-line arguments, forwarded to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(FlaraApiApplication.class, args);
	}

}

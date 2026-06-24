package local.sop.sopinfo.login.test;

import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Test base that starts a MariaDB Testcontainer once and registers JDBC properties
 * into the Spring Test context via @DynamicPropertySource. Tests that need a
 * real MariaDB should extend this class.
 */
public abstract class MariaDBTestBase {

    @SuppressWarnings("resource")
    static final MariaDBContainer<?> maria = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.11"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        // start the container once
        maria.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (maria.isRunning()) {
                    maria.stop();
                }
            } catch (Exception e) {
                // ignore
            }
        }));
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", maria::getJdbcUrl);
        registry.add("spring.datasource.username", maria::getUsername);
        registry.add("spring.datasource.password", maria::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        // Keep security disabled for these controller tests (they mock auth components)
        registry.add("security.enabled", () -> "false");
        // module qualifier used by wiring in this project
        registry.add("bc.qualifier", () -> "login");
    }
}



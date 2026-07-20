package fpt.qn.pms;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(classes = {ProjectManagementSystemApplication.class, BaseIntegrationTest.ContainersConfig.class})
@Transactional
public abstract class BaseIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class ContainersConfig {

        @Bean
        @ServiceConnection
        static PostgreSQLContainer<?> postgresContainer() {
            return new PostgreSQLContainer<>("postgres:18-alpine");
        }

        @Bean
        @ServiceConnection(name = "redis")
        @SuppressWarnings("resource")
        static GenericContainer<?> redisContainer() {
            return new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
        }
    }
}

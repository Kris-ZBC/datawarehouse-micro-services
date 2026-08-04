package local.sop.sopinfo.anonymize.saga.application.config;

import local.sop.common.libs.infrastructure.security.config.MtlsClientFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ServiceClientConfig.class, MockConfig.class)
            .withPropertyValues(
                    "anonymize.base-url=http://anonymize-service",
                    "person.base-url=http://person-service",
                    "auditlog.base-url=http://auditlog-service"
            );

    @Test
    void shouldProvideAllRestClients() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("anonymize");
            assertThat(context).hasBean("person");
            assertThat(context).hasBean("auditlog");

            MtlsClientFactory factory = context.getBean(MtlsClientFactory.class);
            verify(factory).createMtlsClient(eq("anonymize"), eq("http://anonymize-service"));
            verify(factory).createMtlsClient(eq("person"), eq("http://person-service"));
            verify(factory).createMtlsClient(eq("auditlog"), eq("http://auditlog-service"));
        });
    }

    @Test
    void shouldBindPropertiesCorrectly() {
        contextRunner.run(context -> {
            AnonymizeProps anonymizeProps = context.getBean(AnonymizeProps.class);
            assertThat(anonymizeProps.baseUrl()).isEqualTo("http://anonymize-service");

            PersonProps personProps = context.getBean(PersonProps.class);
            assertThat(personProps.baseUrl()).isEqualTo("http://person-service");

            AuditlogProps auditlogProps = context.getBean(AuditlogProps.class);
            assertThat(auditlogProps.baseUrl()).isEqualTo("http://auditlog-service");
        });
    }

    @Configuration
    static class MockConfig {
        @Bean
        MtlsClientFactory mtlsClientFactory() {
            MtlsClientFactory factory = mock(MtlsClientFactory.class);
            when(factory.createMtlsClient(anyString(), anyString())).thenReturn(mock(RestClient.class));
            return factory;
        }
    }
}

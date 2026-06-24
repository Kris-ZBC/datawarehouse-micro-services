package local.sop.sopinfo.infrastructure.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "mtls.enabled", havingValue = "true")
public class MtlsAutoConfiguration {
    @Bean
    MtlsClientFactory mtlsClientFactory(SslBundles sslBundles) {
        return new MtlsClientFactory(sslBundles);
    }

}

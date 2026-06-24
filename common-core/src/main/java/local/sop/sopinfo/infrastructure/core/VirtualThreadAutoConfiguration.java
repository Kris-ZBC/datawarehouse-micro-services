package local.sop.sopinfo.infrastructure.core;

import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

@Configuration
@ConditionalOnProperty(value = "common.threads.virtual.enabled", 
                      havingValue = "true", 
                      matchIfMissing = false) // OPT-IN!
@EnableConfigurationProperties(VirtualThreadProperties.class)
public class VirtualThreadAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatVirtualThreadCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("executor", "virtual-thread-executor");
            // Eller
            connector.getProtocolHandler().setExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
            );
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}

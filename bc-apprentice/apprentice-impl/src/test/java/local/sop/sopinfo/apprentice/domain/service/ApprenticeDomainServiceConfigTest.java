/* package local.sop.sopinfo.apprentice.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ApprenticeDomainServiceConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ApprenticeDomainServiceConfig.class);

    @Test
    void shouldProvideApprenticeDomainBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ApprenticeDomain.class);
            assertThat(context.getBean(ApprenticeDomain.class))
                .isExactlyInstanceOf(ApprenticeDomainService.class);
        });
    }
} */
// This test doesnt follow onion architecture, might need testing through other uses.
package local.sop.sopinfo.interfaceadapters.web.i18nconfig;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;



@SpringBootTest
@ActiveProfiles("test")
class MessageSourceTest {
    
    @Autowired
    private MessageSource messageSource;
    
    @Test
    void shouldFindAllMessages() {
        // Skal nu finde fra alle kilder uden nogen custom I18nAutoConfiguration
        
        // Shared kernel
        String shared = messageSource.getMessage("key.required", null, Locale.forLanguageTag("da"));
        
        // Common web (automatisk med)
        String common = messageSource.getMessage("email.exists", null, Locale.forLanguageTag("da"));
        
        assertNotNull(shared);
        assertNotNull(common);

    }
}

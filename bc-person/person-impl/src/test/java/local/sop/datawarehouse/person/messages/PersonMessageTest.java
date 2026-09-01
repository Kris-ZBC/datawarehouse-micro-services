package local.sop.datawarehouse.person.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "bc.qualifier=person",
    "spring.application.name=person-impl",
    "sop.application.name=datawarehouse"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PersonMessageTest {
    
    @Autowired
    private MessageSource messageSource;
    
    @Test
    void shouldUsePersonSpecificMessage() {
        String message = messageSource.getMessage("person.welcome", null, Locale.forLanguageTag("da"));
        assertEquals("Velkommen til person modulet!", message);
    }
    
    @Test
    void shouldFallbackToCommonMessages() {
        String message = messageSource.getMessage("email.exists", null, Locale.forLanguageTag("da"));
        assertEquals("Email er allerede i brug", message);
    }
    
    @Test
    void shouldFallbackToSharedMessages() {
        String message = messageSource.getMessage("CN.username.invalid", null, Locale.forLanguageTag("da"));
        assertEquals("CN brugernavn er ugyldigt", message);
    }
}
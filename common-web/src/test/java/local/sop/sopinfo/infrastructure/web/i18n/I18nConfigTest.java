package local.sop.sopinfo.infrastructure.web.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/** Test class for I18nConfig. */
 @Profile("test")  // Kun i non-test profiles
public class I18nConfigTest {
    private ReloadableResourceBundleMessageSource ms;
    @BeforeEach
    public void setup() {

        var r = new ReloadableResourceBundleMessageSource();
        r.setBasenames(
            "classpath:i18n/common-core-messages",
            "classpath:i18n/common-security-messages",
            "classpath:i18n/messages"
        );
        r.setDefaultEncoding("UTF-8");
        r.setFallbackToSystemLocale(false);
        r.setUseCodeAsDefaultMessage(false);
        r.setCacheMillis(-1);
        ms = r;
    }

     @Test
    void shared_uuid_keys_resolve_da() {
        var da = Locale.forLanguageTag("da");
        assertEquals("Key er krævet", ms.getMessage("key.required", null, da));
        assertEquals("Key er ugyldig", ms.getMessage("key.invalid", null, da));
        assertEquals("CN brugernavn er krævet", ms.getMessage("CN.username.required", null, da));
        assertEquals("CN brugernavn er ugyldigt", ms.getMessage("CN.username.invalid", null, da));
        assertEquals("CN brugernavn blev ikke fundet", ms.getMessage("CN.username.notfound", null, da));
    }

    @Test
    void shared_uuid_keys_resolve_en() {
        var en = Locale.forLanguageTag("en");
        assertEquals("Key is required", ms.getMessage("key.required", null, en));
        assertEquals("Key is invalid", ms.getMessage("key.invalid", null, en));
        assertEquals("CN username is required", ms.getMessage("CN.username.required", null, en));
        assertEquals("CN username is invalid", ms.getMessage("CN.username.invalid", null, en));
        assertEquals("CN username not found", ms.getMessage("CN.username.notfound", null, en));
    }
}

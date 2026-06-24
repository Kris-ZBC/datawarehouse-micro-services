package local.sop.sopinfo.infrastructure.web.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;


@ExtendWith(MockitoExtension.class)
class SafeAcceptHeaderLocaleResolverTest {

    private SafeAcceptHeaderLocaleResolver resolver;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        resolver = new SafeAcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("da"));
        resolver.setSupportedLocales(java.util.List.of(
            Locale.forLanguageTag("da"),
            Locale.forLanguageTag("en")
        ));
        request = new MockHttpServletRequest();
    }

    @Test
    void defaultLocale_shouldBeDanish() {
        // Given
        // Ingen Accept-Language header

        // When
        Locale locale = resolver.resolveLocale(request);

        // Then
        assertEquals(Locale.forLanguageTag("da"), locale);
    }

    @Test
    void shouldResolveDanishFromHeader() {
        // Given
        request.addHeader("Accept-Language", "da-DK,da;q=0.9,en;q=0.8");

        // When
        Locale locale = resolver.resolveLocale(request);

        // Then
        assertEquals(Locale.forLanguageTag("da"), locale);
    }

    @Test
    void shouldFallbackToDefaultForUnsupportedLanguage() {
        // Given
        request.addHeader("Accept-Language", "fr-FR,de-DE;q=0.9");

        // When
        Locale locale = resolver.resolveLocale(request);

        // Then
        assertEquals(Locale.forLanguageTag("da"), locale);
    }

    @Test
    void shouldHandleMalformedHeader() {
        // Given
        request.addHeader("Accept-Language", "invalid-header-format");

        // When
        Locale locale = resolver.resolveLocale(request);

        // Then
        assertEquals(Locale.forLanguageTag("da"), locale);
    }
}
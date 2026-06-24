package local.sop.sopinfo.infrastructure.web.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;


public class SafeAcceptHeaderLocaleResolver  extends AcceptHeaderLocaleResolver {
    // Define the locales your service actually supports
    private final List<Locale> supported = List.of(
            Locale.forLanguageTag("da"),
            Locale.forLanguageTag("en")
    );

    @Override
    public @NonNull Locale resolveLocale(@NonNull HttpServletRequest request) {
        String header = request.getHeader("Accept-Language");
        // No header -> default
        if (header == null || header.isBlank()) {
            return Objects.requireNonNull(Objects.requireNonNullElse(getDefaultLocale(), Locale.getDefault()));
        }
        try {
            // Try to map to a supported locale (RFC 4647 filtering)
            Locale lookup = Locale.lookup(Locale.LanguageRange.parse(header), supported);
            if (lookup != null) {
                return lookup;
            }
            return Objects.requireNonNull(Objects.requireNonNullElse(getDefaultLocale(), Locale.getDefault()));
        } catch (IllegalArgumentException ex) {
            // Malformed header -> default
            return Objects.requireNonNull(Objects.requireNonNullElse(getDefaultLocale(), Locale.getDefault()));
        }
    }

}

package local.sop.sopinfo.infrastructure.web.i18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.web.servlet.LocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@PropertySource("classpath:application-web.properties") 
public class I18nAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(I18nAutoConfiguration.class);
    
    @Bean
    @Primary  // <-- Gør denne MessageSource til den primære
    public MessageSource messageSource(ResourcePatternResolver resourceResolver) {
        var r = new ReloadableResourceBundleMessageSource();
        
        // Find alle message basenames automatisk
        List<String> basenames = findAllMessageBasenames(resourceResolver);
        
        if (basenames.isEmpty()) {
            // Fallback til kun shared hvis ingen filer findes
            basenames = List.of("classpath:i18n/common-messages");
        }
        
        r.setBasenames(basenames.toArray(new String[0]));
        r.setDefaultEncoding("UTF-8");
        r.setFallbackToSystemLocale(false);
        r.setUseCodeAsDefaultMessage(false);
        r.setCacheMillis(-1);
        
        // Debug output
        log.info("=== Auto-loaded message basenames ===");
        basenames.forEach(basename -> log.info("  " + basename));
        log.info("========================================");
        
        return r;
    }

    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)  // <-- Kun hvis ingen anden findes
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    LocaleResolver localeResolver() {
        var resolver = new SafeAcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("da"));
        return resolver;
    }

    /* privates */
    private List<String> findAllMessageBasenames(ResourcePatternResolver resourceResolver) {
        List<String> basenames = new ArrayList<>();
        
        try {
            // Find ALLE message filer på classpath
            Resource[] resources = resourceResolver.getResources("classpath*:i18n/*_*.properties");
            
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null && filename.endsWith(".properties")) {
                    
                    // Udtræk basename: "person-messages_da.properties" -> "person-messages"
                    String basename = extractBasename(filename);
                    
                    // Tilføj kun unikke basenames
                    if (!basenames.contains(basename)) {
                        basenames.add(basename);
                    }
                }
            }
            
            // Sorter efter korrekt prioritet
            basenames.sort(createPriorityComparator());
            
        } catch (IOException e) {
            log.error("Error scanning for message files: " + e.getMessage());
        }
        
        return basenames;
    }
    
    private String extractBasename(String filename) {
        // Fjern locale suffix: "person-messages_da.properties" -> "person-messages"
        int underscoreIndex = filename.lastIndexOf('_');
        if (underscoreIndex > 0) {
            filename = filename.substring(0, underscoreIndex);
        }
        
        // Fjern .extension
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            filename = filename.substring(0, dotIndex);
        }
        
        return "classpath:i18n/" + filename;
    }
    
    private Comparator<String> createPriorityComparator() {
        return (a, b) -> {
            // Prioritetsrækkefølge:
            // 1. BC-specifikke (person, organization, etc.)
            // 2. Common-web  
            // 3. Shared-kernel
            
            int priorityA = getPriority(a);
            int priorityB = getPriority(b);
            
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB); // Lavere tal = højere prioritet
            }
            
            // Alfabetisk som secondary sortering
            return a.compareTo(b);
        };
    }
    
    private int getPriority(String basename) {
        String lowerName = basename.toLowerCase();
        
        if (lowerName.contains("shared-")) {
            return 3; // Laveste prioritet (fallback)
        } else if (lowerName.contains("common-")) {
            return 2; // Mellemste prioritet
        } else {
            return 1; // Højeste prioritet (BC-specifikke)
        }
    }
}

package local.sop.sopinfo.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import local.sop.sopinfo.infrastructure.security.config.InternalMtlsProps;
import local.sop.sopinfo.infrastructure.security.config.SecurityConfig;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;

@ExtendWith(MockitoExtension.class)
class SecurityConfigMtlsTest {

    @Mock
    private InternalMtlsProps props;
    
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(props);
    }

    @Test
    void shouldHandleEmptyAllowedCallers() {
        // Given
        when(props.allowedCallers()).thenReturn(List.of());

        // When
        UserDetailsService userDetailsService = securityConfig.internalX509UserDetailsService();

        // Then
        assertThrows(NotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("any-service")
        );
    }

    @Test
    void shouldHandleNullAllowedCallers() {
        // Given
        when(props.allowedCallers()).thenReturn(null);

        // When
        UserDetailsService userDetailsService = securityConfig.internalX509UserDetailsService();

        // Then
        assertThrows(NotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("any-service")
        );
    }

    @Test
    void shouldAllowMultipleConfiguredCallers() {
        // Given
        List<String> allowedCallers = List.of("service-a", "service-b", "service-c");
        when(props.allowedCallers()).thenReturn(allowedCallers);

        // When
        UserDetailsService userDetailsService = securityConfig.internalX509UserDetailsService();

        // Then
        allowedCallers.forEach(caller -> {
            UserDetails userDetails = userDetailsService.loadUserByUsername(caller);
            assertEquals(caller, userDetails.getUsername());
            
            // Check authorities
            boolean hasInternalRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_INTERNAL".equals(auth.getAuthority()));
            assertTrue(hasInternalRole);
        });
    }

    @Test
    void shouldCreateImmutableSetOfAllowedCallers() {
        // Given
        List<String> mutableList = new ArrayList<>(List.of("service-a"));
        when(props.allowedCallers()).thenReturn(mutableList);

        // When
        UserDetailsService userDetailsService = securityConfig.internalX509UserDetailsService();

        // Modify original list after UserDetailsService creation
        mutableList.add("service-b");

        // Then
        assertThrows(NotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("service-b")
        );
    }
}
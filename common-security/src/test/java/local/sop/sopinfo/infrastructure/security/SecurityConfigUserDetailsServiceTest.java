package local.sop.sopinfo.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
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


@ExtendWith(MockitoExtension.class)
class SecurityConfigUserDetailsServiceTest {

     @Mock
    private InternalMtlsProps props;
    
    private SecurityConfig securityConfig;
    
    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(props);
    }
    
    @Test
    void shouldAllowConfiguredUser() {
        // Given
        when(props.allowedCallers()).thenReturn(List.of("allowed-service"));
        
        // When
        UserDetailsService userDetailsService = securityConfig.internalX509UserDetailsService();
        
        // Then
        UserDetails userDetails = userDetailsService.loadUserByUsername("allowed-service");
        assertEquals("allowed-service", userDetails.getUsername());
    }

    @Test
    void shouldRejectUnconfiguredUser() {
        // Given
        when(props.allowedCallers()).thenReturn(List.of("allowed-service"));
        
        // When
        UserDetailsService userDetailsService = securityConfig.internalX509UserDetailsService();
        
        // Then
        var ex = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            userDetailsService.loadUserByUsername("disallowed-service");
        });
        assertEquals("CN.username.notfound", ex.getMessage());
    }
}
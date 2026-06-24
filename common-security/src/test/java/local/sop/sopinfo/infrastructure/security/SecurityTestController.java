package local.sop.sopinfo.infrastructure.security;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class SecurityTestController {
    
    @GetMapping("/test")
    public String test() {
        return "Internal test endpoint";
    }
    
    @GetMapping("/uuid/{id}")
    public String testUuid(@PathVariable UUID id) {
        return "UUID: " + id;
    }
    
    @GetMapping("/number/{id}")
    public String testNumber(@PathVariable Long id) {
        return "Number: " + id;
    }
}

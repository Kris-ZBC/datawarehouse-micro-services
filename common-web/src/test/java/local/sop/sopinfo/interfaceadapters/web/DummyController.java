package local.sop.sopinfo.interfaceadapters.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import local.sop.sopinfo.sharedkernel.exceptions.ConcurrencyException;
import local.sop.sopinfo.sharedkernel.exceptions.ConflictException;
import local.sop.sopinfo.sharedkernel.exceptions.InvariantException;
import local.sop.sopinfo.sharedkernel.exceptions.NotFoundException;
import local.sop.sopinfo.sharedkernel.exceptions.PreconditionException;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/__test")
@Validated
class DummyController {

  // ---- DomainException (mapped by your advice) ----
  @GetMapping("/domain-conflict")
  public void domainConflict() {
    throw new ConflictException("email.exists", Map.of("field","email"));
  }

  // ---- @RequestBody bean validation ----
  public record CreateReq(@NotBlank String name) { }

  @PostMapping(path="/body", consumes = "application/json")
  public String create(@Valid @RequestBody CreateReq req) {
    return "ok";
  }

  // ---- Missing parameter ----
  @GetMapping("/needs-q")
  public String needsQ(@RequestParam("q") String q) {
    return q;
  }

   @GetMapping("type/{id}")
    public ResponseEntity<String> testType(@PathVariable Long id) {
        return ResponseEntity.ok("ID: " + id);
    }

  // ---- Type mismatch (?id=abc) ----
  @GetMapping("/type")
  public String type(@RequestParam("id") Long id) {
    return String.valueOf(id);
  }

  // ---- Parameter validation (@Min) -> ConstraintViolationException ----
  @GetMapping("/age")
  public String age(@RequestParam("age") @Min(18) int age) {
    return String.valueOf(age);
  }

  @GetMapping("/not-found")
    public String throwNotFound() {
        throw new NotFoundException("test.not.found", Map.of("id", 123));
    }

    @GetMapping("/validation")
    public String throwValidation() {
        throw new ValidationException("test.validation.failed", Map.of("field","email"));
    }

    @GetMapping("/conflict")
    public String throwConflict() {
        throw new ConflictException("test.conflict", Map.of("resource", "user"));
    }

    @GetMapping("/invariant")
    public String throwInvariant() {
        throw new InvariantException("test.invariant", Map.of("resource", "user"));
    }

    @GetMapping("/precondition")
    public String throwPrecondition() {
        throw new PreconditionException("test.precondition", Map.of("resource", "user"));
    }

    @GetMapping("/concurrency")
    public String throwConcurrency() {
        throw new ConcurrencyException("test.concurrency", Map.of("resource", "user"));
    }


    @PostMapping("/submit")
    public ResponseEntity<String> submitForm(@Validated @ModelAttribute TestForm form, BindingResult result) throws BindException {
        if (result.hasErrors()) {
            throw new BindException(result);
        }
        return ResponseEntity.ok("Success");
    }

    public class TestForm {
    @Min(value = 18)
    private Integer age;
    
    @Email
    private String email;
    
    @NotBlank
    private String name;
    
    // getters/setters
  }

  // ---- DB unique (Postgres/H2 23505) ----
  @GetMapping("/db/unique")
  public void dbUnique() {
    throw new DataIntegrityViolationException("dup", new SQLException("duplicate", "23505", 0));
  }

  // ---- DB unique (SQL Server: SQLState 23000 + vendor 2627) ----
  @GetMapping("/db/sqlserver-unique")
  public void dbSqlServerUnique() {
    throw new DataIntegrityViolationException("dup", new SQLException("violation", "23000", 2627));
  }

  // ---- DB generic conflict fallback ----
  @GetMapping("/db/conflict")
  public void dbConflict() {
    throw new DataIntegrityViolationException("other", new SQLException("other", "99999", 0));
  }

  // ---- Generic error -> fallback 500 ----
  @GetMapping("/boom")
  public void boom() {
    throw new IllegalStateException("boom");
  }
}



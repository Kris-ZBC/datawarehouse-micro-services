package local.sop.sopinfo.infrastructure.web.exception; 

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import local.sop.sopinfo.sharedkernel.exceptions.DomainException;
import local.sop.sopinfo.sharedkernel.exceptions.ErrorCode;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.sql.SQLException;
import java.util.*;

/**
 * Global API exception handler with full i18n support.
 * - Domain exceptions: use domain keys + args (already i18n-ready).
 * - Web/validation exceptions: resolve keys to localized messages.
 * - Fallback 500: generic, localized message.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EndpointExceptionHandler {

    private final MessageSource messages;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EndpointExceptionHandler.class);

    public EndpointExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    // Helper to build ProblemDetail
    private ProblemDetail buildProblemDetail(
            HttpStatus status,
            String type,
            String detail,
            String key,
            Map<String, ?> properties
    ) {
        var pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create(type));
        pd.setDetail(detail);
        pd.setProperty("key", key);
        if (properties != null) {
            properties.forEach(pd::setProperty);
        }
        return pd;
    }

    // 1) DomainException -> ProblemDetail
    @ExceptionHandler(DomainException.class)
    public ProblemDetail onDomain(DomainException ex, Locale locale) {
        HttpStatus status = switch (ex.code()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case INVARIANT -> HttpStatus.UNPROCESSABLE_ENTITY;
            case PRECONDITION -> HttpStatus.PRECONDITION_FAILED;
            case CONFLICT, CONCURRENCY -> HttpStatus.CONFLICT;
        };
        var args = ex.args();
        return buildProblemDetail(
                status,
                typeFromCode(ex.code()),
                interpolate(resolve(ex.messageKey(), locale), args),
                ex.messageKey(),
                Map.of("args", args)
        );
    }

      /** 400 for @RequestBody validation errors (DTO fields) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onMethodArgNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> errors = new LinkedHashMap<>();
        for (var err : ex.getBindingResult().getFieldErrors()) {
        var fe = (FieldError) err;
        errors.put(fe.getField(), fe.getDefaultMessage()); // already i18n-resolved
        }
        var pd = buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "about:blank#validation",                  // keep your previous type to satisfy tests
            "One or more fields are invalid",
            "validation.error",                        // optional key for clients
            Map.of("errors", errors, "instance", req.getRequestURI())
        );
        pd.setTitle("Validation failed");
        return pd;
    }

    /** 400 for binding/type mismatch of query/path parameters and form data */
    @ExceptionHandler(BindException.class)
    public ProblemDetail onBind(BindException ex, HttpServletRequest req) {
        Map<String, Object> errors = new LinkedHashMap<>();
        for (var e : ex.getFieldErrors()) {
        errors.put(e.getField(), e.getDefaultMessage() != null ? e.getDefaultMessage() : "Type mismatch");
        }
        var pd = buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "about:blank#type_mismatch",
            "One or more parameters are invalid",
            "request.param.typeMismatch",
            Map.of("errors", errors, "instance", req.getRequestURI())
        );
        pd.setTitle("Bad Request");
        return pd;
    }

    // 3) @RequestParam/@PathVariable validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail onParamValidation(ConstraintViolationException ex, Locale locale) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), resolveSafe(v.getMessage(), locale)));
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "about:blank#validation",
                resolve("request.param.validation.failed", locale),
                "request.param.validation.failed",
                Map.of("errors", errors)
        );
    }

    // 4) Missing ?param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail onMissingParam(MissingServletRequestParameterException ex, Locale locale) {
        var args = Map.of("param", ex.getParameterName());
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "about:blank#missing_parameter",
                interpolate(resolve("request.param.missing", locale), args),
                "request.param.missing",
                Map.of("args", args)
        );
    }

    /* 
    // 5) Type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail onTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String expected = Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName).orElse("unknown");
        Object value = ex.getValue();
        String actual = (value == null) ? "null" : value.getClass().getSimpleName();
        
        // Get locale from request
        Locale locale = LocaleContextHolder.getLocale();
        
        // Simpel args map til at matche din test
        var args = Map.of("param", ex.getName(), "expected", expected, "actual", actual);
        
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "about:blank#key_invalid",
                interpolate(resolve("key.invalid", locale), args),
                "key.invalid",
                Map.of("param", ex.getName())  // Kun det din test forventer
        );
    }

    */

    /* 5 test */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ProblemDetail onTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String paramName = ex.getName();
    String paramValue = ex.getValue() != null ? ex.getValue().toString() : "null";
    Class<?> requiredType = ex.getRequiredType();
    
    // Bestem om det's query parameter eller path variable baseret på URI
    String requestURI = req.getRequestURI();
    boolean isPathVariable = requestURI.contains("/" + paramValue);
    
    Locale locale = LocaleContextHolder.getLocale();
    
    var args = Map.of("param", paramName, "expected", requiredType.getSimpleName(), "actual", paramValue);
    
    if (isPathVariable) {
        // Path variable - key_invalid
        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "about:blank#type_mismatch",
            interpolate(resolve("request.param.typeMismatch", locale), args),
            "request.param.typeMismatch",
            Map.of("args", Map.of("param", paramName))
        );
    } else {
        // Query parameter - type_mismatch
        return buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "about:blank#type_mismatch",
            interpolate(resolve("request.param.typeMismatch", locale), args),
            "request.param.typeMismatch",
            Map.of("args", Map.of("param", paramName))
        );
    }
}

    // 6) DB conflicts (unique, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail onDataIntegrity(DataIntegrityViolationException ex, Locale locale) {
        Throwable root = mostSpecific(ex);
        if (isUniqueViolation(root)) {
            return buildProblemDetail(
                    HttpStatus.CONFLICT,
                    "about:blank#unique_violation",
                    resolve("unique.violation", locale),
                    "unique.violation",
                    null
            );
        }
        return buildProblemDetail(
                HttpStatus.CONFLICT,
                "about:blank#conflict",
                resolve("conflict", locale),
                "conflict",
                null
        );
    }

     /** 400 for malformed JSON / missing body / unreadable content */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String hint = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        var pd = buildProblemDetail(
            HttpStatus.BAD_REQUEST,
            "about:blank#malformed_json",
            "Malformed JSON request body",
            "request.body.invalid",
            Map.of("hint", hint, "instance", req.getRequestURI())
        );
        pd.setTitle("Bad Request");
        return pd;
    }

    // Handler for endpoints without handler
    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail onNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest req) {
        Locale locale = LocaleContextHolder.getLocale();
        var args = Map.of("path", req.getRequestURI(), "instance", req.getRequestURI());
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "about:blank#no_handler_found",
                interpolate(resolve("endpoint.not.found", locale), args),
                "endpoint.not.found",
                Map.of("path", ex.getRequestURL(), "instance", req.getRequestURI())
        );
    }

     // Handler for endpoints without handler
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail onNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        Locale locale = LocaleContextHolder.getLocale();
        var args = Map.of("path", ex.getResourcePath(), "instance", req.getRequestURI());
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "about:blank#no_resource_found",
                interpolate(resolve("endpoint.not.found", locale), args),
                "endpoint.not.found",
                Map.of("path", ex.getResourcePath(), "instance", req.getRequestURI())
        );
    }

    // 8) Fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail onAny(Exception ex, Locale locale) {
        log.error("Unhandled exception: " + ex.getMessage() /*  + "\n" + Arrays.toString(ex.getStackTrace())*/);
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "about:blank#internal_error",
                resolve("server.error", locale),
                "server.error",
                null
        );
    }

    // ---- helpers ----

    private static Throwable mostSpecific(Throwable t) {
        var cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }

    // SQLServer: SQLState=23000 + vendor 2627/2601
    private static boolean isUniqueViolation(Throwable root) {
        var sql = findSqlException(root);
        if (sql != null) {
            var state = sql.getSQLState();
            var code  = sql.getErrorCode();
            if ("23505".equals(state)) return true;                 // Postgres/H2
            if ("23000".equals(state) && (code == 2627 || code == 2601)) return true; // SQL Server
            if (code == 1062) return true;                          // MySQL
        }
        if ("org.hibernate.exception.ConstraintViolationException".equals(root.getClass().getName()))
            return true;
        var msg = String.valueOf(root.getMessage()).toLowerCase(Locale.ROOT);
        return msg.contains("unique") || msg.contains("duplicate");
    }

    private static SQLException findSqlException(Throwable t) {
        for (var cur = t; cur != null && cur.getCause() != cur; cur = cur.getCause()) {
            if (cur instanceof SQLException se) return se;
        }
        return null;
    }

    private String resolve(String key, Locale locale) {
        log.debug("MessageSource class: " + messages.getClass().getName());
        return messages.getMessage(key, null, key, locale);
    }

    private String resolveSafe(String keyOrLiteral, Locale locale) {
        try { return messages.getMessage(keyOrLiteral, null, keyOrLiteral, locale); }
        catch (Exception ignore) { return keyOrLiteral; }
    }

    private static String interpolate(String template, Map<String,?> args) {
        String s = template;
        for (var e : args.entrySet()) s = s.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        return s;
    }

    private static String typeFromCode(ErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> "about:blank#not_found";
            case VALIDATION -> "about:blank#validation";
            case INVARIANT -> "about:blank#invariant";
            case PRECONDITION -> "about:blank#precondition";
            case CONFLICT -> "about:blank#conflict";
            case CONCURRENCY -> "about:blank#concurrency";
        };
    }
}
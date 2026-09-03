package local.sop.datawarehouse.auditlog.interfaceadapters.persistence.converters;

import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import local.sop.datawarehouse.sharedlib.enums.Severity;

/**
 * Maps between the Java {@link Severity} enum and the lowercase DB ENUM values.
 * <p>
 * DB column: {@code ENUM('info','debug','low','medium','high')}
 * Java enum: {@code DEBUG, INFO, WARNING, ERROR, FATAL}
 * <p>
 * The legacy DB values {@code low/medium/high} map to {@code WARNING/ERROR/FATAL}.
 */
@Converter(autoApply = false)
public class SeverityConverter implements AttributeConverter<Severity, String> {

    private static final Map<Severity, String> TO_DB = Map.of(
            Severity.DEBUG,   "debug",
            Severity.INFO,    "info",
            Severity.WARNING, "low",
            Severity.ERROR,   "medium",
            Severity.FATAL,   "high"
    );

    private static final Map<String, Severity> FROM_DB = Map.of(
            "debug",  Severity.DEBUG,
            "info",   Severity.INFO,
            "low",    Severity.WARNING,
            "medium", Severity.ERROR,
            "high",   Severity.FATAL
    );

    @Override
    public String convertToDatabaseColumn(Severity attribute) {
        if (attribute == null) return null;
        String dbValue = TO_DB.get(attribute);
        if (dbValue == null) {
            throw new IllegalArgumentException("Unknown Severity: " + attribute);
        }
        return dbValue;
    }

    @Override
    public Severity convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        Severity severity = FROM_DB.get(dbData.toLowerCase());
        if (severity == null) {
            throw new IllegalArgumentException("Unknown DB severity value: " + dbData);
        }
        return severity;
    }
}

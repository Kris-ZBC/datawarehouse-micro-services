package local.sop.sopinfo.workhour.domain.model.valueobjects;

import java.util.regex.Pattern;
import java.util.Map;
import java.util.StringJoiner;

import local.sop.common.libs.sharedkernel.exceptions.ValidationException;

public record WorkScheduleTime(String time) {
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");

    public WorkScheduleTime {
        if(time == null || time.isBlank() || time.isEmpty() || !TIME_PATTERN.matcher(time.trim()).matches()) {
            throw new ValidationException("time.invalid", Map.of("field", time==null? "time": time));
        }
        time = time.trim();
    }

    public static WorkScheduleTime of(String time) {
        return new WorkScheduleTime(time);
    }

     @Override
    public String toString() {
        return new StringJoiner(",", getClass().getSimpleName() + "{", "}")
            .add("time: "+ (time == null? null : time))
        .toString();
    }
}

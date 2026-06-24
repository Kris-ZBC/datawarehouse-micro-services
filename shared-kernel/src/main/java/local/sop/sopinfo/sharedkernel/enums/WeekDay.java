package local.sop.sopinfo.sharedkernel.enums;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

public enum WeekDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public static WeekDay parse(String day) throws ValidationException {
        Logger log = LoggerFactory.getLogger(WeekDay.class);
        if(day == null) {
            log.warn("day is null!");
            throw new ValidationException("weekday.invalid", Map.of("field", "day"));

        } 
        try {
            return valueOf(day);
        }
        catch(IllegalArgumentException ex) {
            log.warn("day is invalid {}", day);
            throw new ValidationException("weekday.invalid", Map.of("field", "day"));
        }
    }
}

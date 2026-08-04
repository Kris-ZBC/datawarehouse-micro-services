package local.sop.sopinfo.workhour.domain.model;

import java.util.Map;
import java.util.StringJoiner;

import local.sop.common.libs.sharedkernel.enums.WeekDay;
import local.sop.common.libs.sharedkernel.exceptions.ValidationException;
import local.sop.sopinfo.workhour.domain.model.valueobjects.*;

/**
 * Aggregate root
 */  

public class WorkHourSchedule {
    private final WorkScheduleId id;
    private final WorkScheduleTime startTime;
    private final WorkScheduleTime endTime;
    private final WeekDay weekDay;
    private final SopRef sopRef;

    private WorkHourSchedule(
        WorkScheduleId id, WorkScheduleTime startTime,
        WorkScheduleTime endTime, WeekDay weekDay, SopRef sopRef) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
            this.weekDay = weekDay;
            this.sopRef = sopRef;
    }

    public WorkHourSchedule withStartTime(WorkScheduleTime time) {
        return new WorkHourSchedule(this.id, time, this.endTime, this.weekDay, this.sopRef);
    }

    public WorkHourSchedule withEndTime(WorkScheduleTime time) {
        return new WorkHourSchedule(this.id, this.startTime, time, this.weekDay, this.sopRef);
    }

    public WorkHourSchedule withWeekDay(WeekDay day) {
        return new WorkHourSchedule(this.id, this.startTime, this.endTime, day, this.sopRef);
    }

    public WorkHourSchedule withSopRef(SopRef sopRef) {
        return new WorkHourSchedule(this.id, this.startTime, this.endTime, this.weekDay, sopRef);
    }

    /* Getters */
    public WorkScheduleId getId() {return this.id;}
    public WorkScheduleTime getStartTime() {return this.startTime;}
    public WorkScheduleTime getEndTime() {return this.endTime;}
    public WeekDay getWeekDay() {return this.weekDay;}
    public SopRef getSopRef() {return this.sopRef;}

    @Override
    public String toString() {
        return new StringJoiner(",", getClass().getSimpleName() + "{", "}")
            .add("id: "+ (id == null? null : id.value().toString())).add(String.valueOf('\n'))
            .add("startTime: "+ (startTime == null? null : startTime.time())).add(String.valueOf('\n'))
            .add("endTime: "+ (endTime == null? null : endTime.time())).add(String.valueOf('\n'))
            .add("weekDay: "+ (weekDay == null? null : weekDay.name())).add(String.valueOf('\n'))
            .add("sopRef: "+ (sopRef == null? null : sopRef.value().toString())).add(String.valueOf('\n'))
        .toString();
    }

    /* Builder factory inner class */

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private WorkScheduleId id = null;
        private WorkScheduleTime startTime = null;
        private WorkScheduleTime endTime = null;
        private WeekDay weekDay = null;
        private SopRef sopRef = null;

        public Builder id(WorkScheduleId id) {this.id = id; return this;}
        public Builder startTime(WorkScheduleTime s) {this.startTime = s; return this;}
        public Builder endTime(WorkScheduleTime s) {this.endTime = s; return this;}
        public Builder weekDay(WeekDay w) {this.weekDay = w; return this;}
        public Builder sopRef(SopRef s) {this.sopRef = s; return this;}

        public WorkHourSchedule build() {
            if(id == null) this.id = WorkScheduleId.newId();
            if(startTime == null) throw new ValidationException("time.invalid", Map.of("field", "startTime"));
            if(endTime == null) throw new ValidationException("time.invalid", Map.of("field", "endTime"));
            if(weekDay == null) throw new ValidationException("weekday.invalid", Map.of("field", "weekDay"));
            if(sopRef == null) throw new ValidationException("sopref.invalid", Map.of("field", "sopRef"));
            return new WorkHourSchedule(id, startTime, endTime, weekDay, sopRef);
        }
    }
}

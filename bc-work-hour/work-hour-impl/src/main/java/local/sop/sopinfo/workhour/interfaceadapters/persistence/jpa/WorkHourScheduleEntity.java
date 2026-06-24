package local.sop.sopinfo.workhour.interfaceadapters.persistence.jpa;

import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Column;
import local.sop.sopinfo.sharedkernel.enums.WeekDay;
import local.sop.sopinfo.sharedkernel.exceptions.ValidationException;

@Entity
@Table(name="work_hour_schedule")
public class WorkHourScheduleEntity {
    @Id
    private UUID id;

    @Version
    private Long version;
    
    @Column(name="start_time", nullable=false)
    private String startTime;

    @Column(name="end_time", nullable=false)
    private String endTime;

    @Column(name="week_day", nullable=false)
    @Enumerated(EnumType.STRING)
    private WeekDay weekDay;

    @Column(name="sop_id", nullable=false)
    private UUID sopRef;

    protected WorkHourScheduleEntity() {} //JPA requirement

    private WorkHourScheduleEntity(UUID id, String startTime, String endTime, WeekDay weekDay, UUID sopRef) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekDay = weekDay;
        this.sopRef = sopRef;
    }

    /** Withers */
    
    public WorkHourScheduleEntity withStartTime(String time) {
        return new WorkHourScheduleEntity(this.id, time, this.endTime, this.weekDay, this.sopRef);
    }

    public WorkHourScheduleEntity withEndTime(String time) {
        return new WorkHourScheduleEntity(this.id, this.startTime, time, this.weekDay, this.sopRef);
    }

    public WorkHourScheduleEntity withWeekDay(WeekDay day) {
        return new WorkHourScheduleEntity(this.id, this.startTime, this.endTime, day, this.sopRef);
    }

    public WorkHourScheduleEntity withSopRef(UUID sopRef) {
        return new WorkHourScheduleEntity(this.id, this.startTime, this.endTime, this.weekDay, sopRef);
    }

    /** Getters */
    public UUID getId() {return this.id;}
    public String getStartTime() {return this.startTime;}
    public String getEndTime() {return this.endTime;}
    public WeekDay getWeekDay() {return this.weekDay;}
    public UUID getSopRef() {return this.sopRef;}

     /* Builder factory inner class */
    
    public static Builder builder() {return new Builder();}

    public static class Builder {
        private UUID id;
        private String startTime;
        private String endTime;
        private WeekDay weekDay;
        private UUID sopRef;

        public Builder id(UUID id) {
            if(id == null) throw new ValidationException("key.invalid", Map.of("field", "id"));
            this.id = id; 
            return this;
        }
        public Builder startTime(String s) {
            if(s == null) throw new ValidationException("time.invalid", Map.of("field", "startTime"));
            this.startTime = s; 
            return this;
        }
        public Builder endTime(String s) {
            if(s == null) throw new ValidationException("time.invalid", Map.of("field", "endTime"));
            this.endTime = s; 
            return this;
        }
        public Builder weekDay(WeekDay w) {
            if(w == null) throw new ValidationException("weekday.invalid", Map.of("field", "weekDay"));
            this.weekDay = w; 
            return this;
        }
        public Builder sopRef(UUID s) {
            if(s == null) throw new ValidationException("sopref.invalid", Map.of("field", "sopRef"));
            this.sopRef = s; 
            return this;
        }

        public WorkHourScheduleEntity build() {
            return new WorkHourScheduleEntity(id, startTime, endTime, weekDay, sopRef);
        }
    }    

}

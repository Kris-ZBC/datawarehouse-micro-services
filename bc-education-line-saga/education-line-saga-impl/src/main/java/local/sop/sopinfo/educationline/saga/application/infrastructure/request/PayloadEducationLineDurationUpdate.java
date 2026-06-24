package local.sop.sopinfo.educationline.saga.application.infrastructure.request;

public record PayloadEducationLineDurationUpdate(
    int durationYears,
    int durationMonths,
    int durationDays
) { }

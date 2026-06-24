package local.sop.sopinfo.education.saga.application.infrastructure.request;

public record PayloadEducationCreate(
    String name,
    String category
) {}
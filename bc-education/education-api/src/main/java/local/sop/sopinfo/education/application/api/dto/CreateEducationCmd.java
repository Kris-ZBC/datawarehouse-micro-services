package local.sop.sopinfo.education.application.api.dto;

public record CreateEducationCmd(
        String name,
        String category
) {}
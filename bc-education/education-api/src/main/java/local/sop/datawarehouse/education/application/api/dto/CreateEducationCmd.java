package local.sop.datawarehouse.education.application.api.dto;

public record CreateEducationCmd(
        String name,
        String category
) {}
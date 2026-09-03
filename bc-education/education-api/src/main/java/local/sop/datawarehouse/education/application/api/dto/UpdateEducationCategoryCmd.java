package local.sop.datawarehouse.education.application.api.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEducationCategoryCmd(
    @NotNull(message="{education.category.invalid}")
    @NotBlank(message="{education.category.invalid}")
    @Size(max=100, message="{education.category.invalid}")
    String category
) {}

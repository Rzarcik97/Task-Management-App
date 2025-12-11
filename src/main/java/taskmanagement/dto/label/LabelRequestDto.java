package taskmanagement.dto.label;

import jakarta.validation.constraints.NotBlank;

public record LabelRequestDto(
        @NotBlank String name,
        @NotBlank String color
) {}

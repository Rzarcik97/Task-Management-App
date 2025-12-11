package taskmanagement.dto.project;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProjectRequestDto(
        @NotNull String name,
        @NotNull String description,
        LocalDate endDate) {
}

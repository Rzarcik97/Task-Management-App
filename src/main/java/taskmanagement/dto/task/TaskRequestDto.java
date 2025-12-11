package taskmanagement.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import taskmanagement.model.Task;

public record TaskRequestDto(
        @NotBlank String name,
        @NotNull String description,
        @NotNull Task.Priority priority,
        @NotNull Task.Status status,
        @NotNull LocalDate dueDate,
        @NotNull Long projectId,
        @NotNull String assigneeEmail,
        @NotNull Set<Long> labelIds
) {
}

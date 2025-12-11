package taskmanagement.dto.task;

import java.time.LocalDate;
import java.util.Set;
import taskmanagement.model.Task;

public record TaskResponseDto(
        Long id,
        String name,
        String description,
        Task.Priority priority,
        Task.Status status,
        LocalDate dueDate,
        String projectName,
        String assigneeEmail,
        Set<String> labels
) {
}

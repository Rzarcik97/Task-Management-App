package taskmanagement.dto.task;

import java.time.LocalDate;
import java.util.Set;
import taskmanagement.model.Task;

public record TaskPatchRequestDto(
        String name,
        String description,
        Task.Priority priority,
        Task.Status status,
        LocalDate dueDate,
        String assigneeEmail,
        Set<Long> labelIds) {
}

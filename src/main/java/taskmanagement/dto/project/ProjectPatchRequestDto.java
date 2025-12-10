package taskmanagement.dto.project;

import java.time.LocalDate;
import taskmanagement.model.Project;

public record ProjectPatchRequestDto(
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Project.Status status
) {}

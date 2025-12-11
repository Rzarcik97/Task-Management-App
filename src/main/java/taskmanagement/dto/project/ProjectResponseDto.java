package taskmanagement.dto.project;

import java.time.LocalDate;
import java.util.List;
import taskmanagement.dto.projectmember.ProjectMemberDto;
import taskmanagement.model.Project;

public record ProjectResponseDto(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Project.Status status,
        List<ProjectMemberDto> members
) {}

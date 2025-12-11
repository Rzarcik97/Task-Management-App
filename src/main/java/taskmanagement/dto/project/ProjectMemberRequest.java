package taskmanagement.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import taskmanagement.model.ProjectMember;

public record ProjectMemberRequest(
        @NotBlank String memberEmail,
        @NotNull ProjectMember.Role role
) {
}

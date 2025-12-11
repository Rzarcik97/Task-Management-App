package taskmanagement.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserRoleUpdateRequestDto(
        @NotBlank String role
) {}

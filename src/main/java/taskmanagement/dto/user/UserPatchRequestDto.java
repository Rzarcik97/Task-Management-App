package taskmanagement.dto.user;

public record UserPatchRequestDto(
        String username,
        String firstName,
        String lastName
) {}

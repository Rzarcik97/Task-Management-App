package taskmanagement.dto.comment;

import jakarta.validation.constraints.NotNull;

public record CommentRequestDto(
        @NotNull Long taskId,
        @NotNull String text
) {}

package taskmanagement.dto.comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String taskName,
        String authorUsername,
        String text,
        LocalDateTime timestamp
) {}

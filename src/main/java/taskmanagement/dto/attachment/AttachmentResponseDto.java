package taskmanagement.dto.attachment;

import java.time.LocalDateTime;

public record AttachmentResponseDto(
        Long id,
        String taskName,
        String filename,
        String dropboxFileId,
        String path,
        LocalDateTime uploadDate,
        String uploadedBy
) {}

package taskmanagement.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import taskmanagement.dto.attachment.AttachmentResponseDto;

public interface AttachmentService {

    AttachmentResponseDto uploadAttachment(Long taskId, MultipartFile file, String email);

    List<AttachmentResponseDto> getAttachmentsByTask(Long taskId, String email);

    ResponseEntity<byte[]> downloadAttachment(Long attachmentId, String email);

    void deleteAttachment(Long attachmentId, String email);
}

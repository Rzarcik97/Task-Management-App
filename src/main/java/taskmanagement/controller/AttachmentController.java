package taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import taskmanagement.dto.attachment.AttachmentResponseDto;
import taskmanagement.service.AttachmentService;

@Log4j2
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(path = "/{taskId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload Attachment",
            description = "Upload a file to Dropbox and store reference "
                    + "(Dropbox File ID and path) in database")
    public AttachmentResponseDto uploadAttachment(@PathVariable Long taskId,
                                                  @RequestPart("file") MultipartFile file,
                                                  Authentication authentication) {
        String email = authentication.getName();
        log.info("User {} uploading Attachment to task {}", email, taskId);
        return attachmentService.uploadAttachment(taskId, file, email);
    }

    @GetMapping("/{taskId}")
    @PageableAsQueryParam
    @Operation(summary = "Get Task Attachments",
            description = "Retrieve all attachments for a given task."
                    + " Each attachment contains Dropbox File ID, which"
                    + " can be used to fetch the file.")
    public List<AttachmentResponseDto> getAttachmentsByTask(@PathVariable("taskId") Long taskId,
                                                            Authentication authentication,
                                                            @ParameterObject Pageable pageable) {
        String email = authentication.getName();
        return attachmentService.getAttachmentsByTask(taskId, email, pageable);
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download Attachment",
            description = "Download the actual file from Dropbox by Attachment ID")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId,
                                                     Authentication authentication) {
        String email = authentication.getName();
        return attachmentService.downloadAttachment(attachmentId, email);
    }

    @DeleteMapping("/{attachmentId}/delete")
    @Operation(summary = "Delete Attachment",
            description = "Delete the actual file from Dropbox by Attachment ID")
    public void deleteAttachment(@PathVariable Long attachmentId,
                                                     Authentication authentication) {
        String email = authentication.getName();
        log.info("User {} deleting Attachment {}", email, attachmentId);
        attachmentService.deleteAttachment(attachmentId, email);
    }
}

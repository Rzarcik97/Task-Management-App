package taskmanagement.service.impl;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import taskmanagement.dto.attachment.AttachmentResponseDto;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.exceptions.FileStorageException;
import taskmanagement.mapper.AttachmentMapper;
import taskmanagement.model.Attachment;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.AttachmentRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.AttachmentService;
import taskmanagement.service.dropbox.DropboxService;

@Log4j2
@RequiredArgsConstructor
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PermissionValidator permissionValidator;
    private final DropboxService dropboxService;
    private final AttachmentMapper attachmentMapper;

    @Override
    public AttachmentResponseDto uploadAttachment(Long taskId, MultipartFile file, String email) {
        log.info("starting uploading Attachment to task {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + taskId + " not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email " + email + " not found"));
        try {
            permissionValidator.validateAccess(email,
                    task.getProject().getId(),
                    ProjectMember.Role.MANAGER);
        } catch (AccessDeniedException ex) {
            log.info("checking assigment permissions");
            if (!task.getAssignee().equals(user)) {
                throw new AccessDeniedException(
                        "You don't have permission to upload attachment to this task");
            }
            log.info("access granted to upload attachment");
        }
        FileMetadata metadata;
        try {
            metadata = dropboxService.uploadFile(file, "/tasks/" + taskId);
        } catch (IOException | DbxException e) {
            throw new FileStorageException(
                    "Failed to upload file to Dropbox: " + e.getMessage(), e);
        }
        if (!attachmentRepository.findByPath(metadata.getPathLower()).isEmpty()) {
            throw new FileStorageException(
                    "File with path " + metadata.getPathLower() + " already exists");
        }
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setDropboxFileId(metadata.getId());
        attachment.setFilename(file.getOriginalFilename());
        attachment.setPath(metadata.getPathLower());
        attachment.setUploadDate(LocalDateTime.now());
        attachment.setUploadedBy(user);
        Attachment saved = attachmentRepository.save(attachment);
        log.info("Attachment uploaded successfully: id={}", saved.getId());
        return attachmentMapper.toDto(saved);
    }

    @Override
    public List<AttachmentResponseDto> getAttachmentsByTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + taskId + " not found"));
        permissionValidator.validateAccess(email,
                task.getProject().getId(),
                ProjectMember.Role.VIEWER);
        return attachmentRepository.findByTask_Id(taskId).stream()
                .map(attachmentMapper::toDto)
                .toList();
    }

    @Override
    public ResponseEntity<byte[]> downloadAttachment(Long attachmentId, String email) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Attachment with id " + attachmentId + " not found"));

        permissionValidator.validateAccess(email,
                attachment.getTask().getProject().getId(),
                ProjectMember.Role.VIEWER);

        byte[] fileData = dropboxService.downloadFile(attachment.getDropboxFileId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
    }

    @Override
    public void deleteAttachment(Long attachmentId, String email) {
        log.info("starting deleting Attachment: id = {}", attachmentId);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Attachment with id " + attachmentId + " not found"));
        Task task = attachment.getTask();
        try {
            permissionValidator.validateAccess(email,
                    task.getProject().getId(),
                    ProjectMember.Role.MANAGER);
        } catch (AccessDeniedException ex) {
            log.info("checking ownership permissions");
            if (!attachment.getUploadedBy().getEmail().equals(email)) {
                throw new AccessDeniedException(
                        "You don't have permission to upload attachment to this task");
            }
            log.info("access granted to delete attachment");
        }
        try {
            dropboxService.deleteFile(attachment.getPath());
        } catch (FileStorageException e) {
            throw new FileStorageException(
                    "Failed to delete file from Dropbox: " + e.getMessage(), e);
        }

        attachmentRepository.delete(attachment);
        log.info("Attachment deleted successfully");
    }
}

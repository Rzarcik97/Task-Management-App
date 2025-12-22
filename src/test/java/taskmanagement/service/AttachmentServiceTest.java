package taskmanagement.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dropbox.core.v2.files.FileMetadata;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import taskmanagement.dto.attachment.AttachmentResponseDto;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.exceptions.FileStorageException;
import taskmanagement.mapper.AttachmentMapper;
import taskmanagement.mapper.impl.AttachmentMapperImpl;
import taskmanagement.model.Attachment;
import taskmanagement.model.Project;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.AttachmentRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.dropbox.DropboxService;
import taskmanagement.service.impl.AttachmentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionValidator permissionValidator;
    @Mock
    private DropboxService dropboxService;
    @Mock
    private MultipartFile file;

    @Spy
    private AttachmentMapper attachmentMapper = new AttachmentMapperImpl();

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @Test
    @DisplayName("""
            uploadAttachment | manager should successfully upload attachment
            """)
    void uploadAttachment_managerRoleAccess_success() throws Exception {
        // given
        Long taskId = 1L;
        String email = "manager@example.com";

        Project project = new Project();
        project.setId(1L);

        User user = new User();
        user.setEmail(email);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        FileMetadata metadata = mock(FileMetadata.class);

        when(metadata.getId()).thenReturn("fileId");
        when(metadata.getPathLower()).thenReturn("/tasks/1/test.txt");

        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(dropboxService.uploadFile(file, "/tasks/" + taskId))
                .thenReturn(metadata);
        when(attachmentRepository.findByPath("/tasks/1/test.txt"))
                .thenReturn(List.of());
        Attachment saved = new Attachment();
        saved.setId(1L);

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(saved);

        // when
        AttachmentResponseDto result = attachmentService.uploadAttachment(taskId, file, email);

        // then
        assertEquals(saved.getId(), result.id());
        verify(taskRepository).findById(taskId);
        verify(userRepository).findByEmail(email);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(dropboxService).uploadFile(file, "/tasks/" + taskId);
        verify(attachmentRepository).save(any());
    }

    @Test
    @DisplayName("""
            uploadAttachment | assignee should upload when manager access denied
            """)
    void uploadAttachment_assigneeRoleAccess_success() throws Exception {
        // given
        Long taskId = 1L;
        String email = "assignee@example.com";

        User assignee = new User();
        assignee.setEmail(email);

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setAssignee(assignee);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(assignee));
        doThrow(new AccessDeniedException("not manager"))
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        FileMetadata metadata = mock(FileMetadata.class);
        when(metadata.getId()).thenReturn("fileId");
        when(metadata.getPathLower()).thenReturn("/tasks/1/test.txt");

        when(file.getOriginalFilename()).thenReturn("test.txt");

        when(dropboxService.uploadFile(file, "/tasks/" + taskId)).thenReturn(metadata);
        when(attachmentRepository.findByPath("/tasks/1/test.txt")).thenReturn(List.of());

        Attachment saved = new Attachment();
        saved.setId(1L);

        when(attachmentRepository.save(any())).thenReturn(saved);

        // when
        AttachmentResponseDto result = attachmentService.uploadAttachment(taskId, file, email);

        // then
        assertEquals(saved.getId(), result.id());
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(attachmentRepository).save(any());
    }

    @Test
    @DisplayName("""
            uploadAttachment | should throw when task not found
            """)
    void uploadAttachment_taskNotFound() {
        //given
        String email = "manager@example.com";

        Task task = new Task();
        task.setId(1L);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());
        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.uploadAttachment(task.getId(), file, email));

        verify(taskRepository).findById(task.getId());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("""
            uploadAttachment | should throw when user not found
            """)
    void uploadAttachment_userNotFound() {
        //given
        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        String email = "manager@example.com";

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        //when + then
        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.uploadAttachment(task.getId(), file, email));

        verify(taskRepository).findById(task.getId());
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(dropboxService);
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            uploadAttachment | should throw FileStorageException on Dropbox failure
            """)
    void uploadAttachment_dropboxUploadError() throws Exception {
        //given
        String email = "manager@example.com";

        User user = new User();
        user.setEmail(email);

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        when(dropboxService.uploadFile(any(), any()))
                .thenThrow(new IOException("BOOM"));

        //when +then
        assertThrows(FileStorageException.class,
                () -> attachmentService.uploadAttachment(task.getId(), file, email));

        verify(taskRepository).findById(task.getId());
        verify(userRepository).findByEmail(email);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(dropboxService).uploadFile(any(), any());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            uploadAttachment | should throw FileStorageException when file path already exists
            """)
    void uploadAttachment_duplicatePath() throws Exception {
        //given
        String email = "manager@example.com";

        User user = new User();
        user.setEmail(email);

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(permissionValidator)
                .validateAccess(any(), any(), any());

        FileMetadata metadata = mock(FileMetadata.class);
        when(metadata.getPathLower()).thenReturn("/tasks/1/test.txt");
        when(dropboxService.uploadFile(file, "/tasks/1")).thenReturn(metadata);
        when(attachmentRepository.findByPath("/tasks/1/test.txt"))
                .thenReturn(List.of(new Attachment()));
        //when + then
        assertThrows(FileStorageException.class,
                () -> attachmentService.uploadAttachment(task.getId(), file, email));

        verify(taskRepository).findById(task.getId());
        verify(userRepository).findByEmail(email);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(dropboxService).uploadFile(file, "/tasks/1");
        verify(attachmentRepository).findByPath("/tasks/1/test.txt");
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            getAttachmentsByTask | should return list of attachments
            """)
    void getAttachmentsByTask_success() {
        // given
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setName("Test Task");

        Attachment attachment1 = new Attachment();
        attachment1.setId(1L);
        Attachment attachment2 = new Attachment();
        attachment2.setId(2L);

        String email = "viewer@example.com";

        AttachmentResponseDto dto1 = new AttachmentResponseDto(
                attachment1.getId(),
                task.getName(),
                "file1.txt",
                "p1",
                null,
                null,
                null);
        AttachmentResponseDto dto2 = new AttachmentResponseDto(
                attachment2.getId(),
                task.getName(),
                "file2.txt",
                "p2",
                null,
                null,
                null);
        Page<Attachment> page = new PageImpl<>(List.of(attachment1, attachment2));
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        when(attachmentRepository.findByTask_Id(taskId,pageable))
                .thenReturn(page);
        when(attachmentMapper.toDto(attachment1)).thenReturn(dto1);
        when(attachmentMapper.toDto(attachment2)).thenReturn(dto2);

        // when
        List<AttachmentResponseDto> result =
                attachmentService.getAttachmentsByTask(taskId, email, pageable);

        // then
        assertEquals(2, result.size());
        assertEquals(attachment1.getId(), result.get(0).id());
        assertEquals(attachment2.getId(), result.get(1).id());

        // verify
        verify(taskRepository).findById(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        verify(attachmentRepository).findByTask_Id(taskId,pageable);
        verify(attachmentMapper).toDto(attachment1);
        verify(attachmentMapper).toDto(attachment2);
    }

    @Test
    @DisplayName("""
            getAttachmentsByTask |  should throw EntityNotFoundException when task not found
            """)
    void getAttachmentsByTask_taskNotFound() {
        // given
        Long taskId = 1L;
        String email = "viewer@example.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.getAttachmentsByTask(taskId, email, Pageable.unpaged()));

        // verify
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(attachmentRepository);
        verifyNoInteractions(attachmentMapper);
    }

    @Test
    @DisplayName("""
            downloadAttachment | should return file data with correct headers
            """)
    void downloadAttachment_success() {
        // given
        Long attachmentId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setProject(project);

        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setTask(task);
        attachment.setPath("/dbx123");
        attachment.setFilename("test.pdf");

        byte[] expectedBytes = "file data".getBytes();

        String email = "viewer@example.com";

        when(attachmentRepository.findById(attachmentId))
                .thenReturn(Optional.of(attachment));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        when(dropboxService.downloadFile(attachment.getPath()))
                .thenReturn(expectedBytes);
        // when
        ResponseEntity<byte[]> response =
                attachmentService.downloadAttachment(attachmentId, email);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "attachment; filename=\"test.pdf\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
        );
        assertArrayEquals(expectedBytes, response.getBody());

        // verify
        verify(attachmentRepository).findById(attachmentId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        verify(dropboxService).downloadFile(attachment.getPath());
    }

    @Test
    @DisplayName("""
            downloadAttachment | should throw EntityNotFoundException when attachment not found
            """)
    void downloadAttachment_attachmentNotFound() {
        // given
        Long attachmentId = 1L;
        String email = "viewer@example.com";

        when(attachmentRepository.findById(attachmentId))
                .thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.downloadAttachment(attachmentId, email));

        // verify
        verify(attachmentRepository).findById(attachmentId);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(dropboxService);
    }

    @Test
    @DisplayName("""
            deleteAttachment | should delete file and attachment when valid entry
            """)
    void deleteAttachment_success_manager() {
        // given
        Project project = new Project();
        project.setId(1L);

        User uploader = new User();
        uploader.setEmail("john@example.com");

        Task task = new Task();
        task.setProject(project);

        String path = "/dropbox/path/file.pdf";

        Long attachmentId = 1L;

        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setTask(task);
        attachment.setPath(path);
        attachment.setUploadedBy(uploader);

        String email = "manager@example.com";

        when(attachmentRepository.findById(attachmentId))
                .thenReturn(Optional.of(attachment));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        // when
        attachmentService.deleteAttachment(attachmentId, email);

        // then
        verify(attachmentRepository).findById(attachmentId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(dropboxService).deleteFile(path);
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    @DisplayName("""
            deleteAttachment | should skip manager permission and delete task
             when requested as uploader
            """)
    void deleteAttachment_success_uploader() {
        // given
        String email = "uploader@example.com";

        Project project = new Project();
        project.setId(1L);

        User uploader = new User();
        uploader.setEmail(email);

        Task task = new Task();
        task.setProject(project);

        String path = "/dropbox/path/file.pdf";

        Long attachmentId = 1L;

        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setTask(task);
        attachment.setPath(path);
        attachment.setUploadedBy(uploader);

        when(attachmentRepository.findById(attachmentId))
                .thenReturn(Optional.of(attachment));

        doThrow(new AccessDeniedException("no access"))
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        // when
        attachmentService.deleteAttachment(attachmentId, email);

        // then
        verify(attachmentRepository).findById(attachmentId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(dropboxService).deleteFile(path);
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    @DisplayName("""
            deleteAttachment | should throw EntityNotFoundException when attachment not found
            """)
    void deleteAttachment_notFound() {
        // given
        Long attachmentId = 1L;
        String email = "manager@example.com";

        when(attachmentRepository.findById(attachmentId))
                .thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.deleteAttachment(attachmentId, email));

        // verify
        verify(attachmentRepository).findById(attachmentId);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(dropboxService);
    }

    @Test
    @DisplayName("""
            deleteAttachment | should throw FileStorageException when dropbox failure
            """)
    void deleteAttachment_dropboxFails() {
        // given

        Project project = new Project();
        project.setId(1L);

        User uploader = new User();
        uploader.setEmail("john@example.com");

        Task task = new Task();
        task.setProject(project);

        String path = "/dropbox/path/file.pdf";

        Long attachmentId = 1L;

        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setTask(task);
        attachment.setUploadedBy(uploader);
        attachment.setPath(path);

        String email = "manager@example.com";

        when(attachmentRepository.findById(attachmentId))
                .thenReturn(Optional.of(attachment));

        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        doThrow(new FileStorageException("dropbox fail"))
                .when(dropboxService)
                .deleteFile(path);

        // when + then
        assertThrows(FileStorageException.class,
                () -> attachmentService.deleteAttachment(attachmentId, email));

        // verify
        verify(attachmentRepository).findById(attachmentId);
        verify(dropboxService).deleteFile(path);
        verify(attachmentRepository, never()).delete(any());
    }
}

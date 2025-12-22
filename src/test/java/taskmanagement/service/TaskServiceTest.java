package taskmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import taskmanagement.dto.task.TaskPatchRequestDto;
import taskmanagement.dto.task.TaskRequestDto;
import taskmanagement.dto.task.TaskResponseDto;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.TaskMapper;
import taskmanagement.mapper.impl.TaskMapperImpl;
import taskmanagement.model.Label;
import taskmanagement.model.Project;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.LabelRepository;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.impl.TaskServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private TaskMapper taskMapper = new TaskMapperImpl();

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TaskServiceImpl taskServiceImpl;

    @Test
    @DisplayName("""
            createTask | task created successfully, labels OK,
            assignee found, email sent
            """)
    void createTask_validRequest_success() {
        // given
        Label label1 = new Label();
        label1.setId(1L);
        label1.setName("frontend");
        Label label2 = new Label();
        label2.setId(2L);
        label1.setName("backend");

        Long projectId = 1L;

        TaskRequestDto request = new TaskRequestDto(
                "New task",
                "Some description",
                Task.Priority.HIGH,
                Task.Status.IN_PROGRESS,
                LocalDate.of(2025, 1, 10),
                projectId,
                "john@example.com",
                Set.of(label1.getId(), label2.getId())
        );

        Project project = new Project();
        project.setId(projectId);
        project.setName("Project");

        User assignee = new User();
        assignee.setEmail(request.assigneeEmail());

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setName(request.name());
        savedTask.setLabels(Set.of(label1, label2));
        savedTask.setAssignee(assignee);
        savedTask.setProject(project);

        Set<String> expectedLabels = new HashSet<>();
        expectedLabels.add(label1.getName());
        expectedLabels.add(label2.getName());

        TaskResponseDto expectedDto = new TaskResponseDto(
                savedTask.getId(),
                savedTask.getName(),
                savedTask.getDescription(),
                savedTask.getPriority(),
                savedTask.getStatus(),
                savedTask.getDueDate(),
                savedTask.getProject().getName(),
                savedTask.getAssignee().getEmail(),
                expectedLabels
        );

        String email = "manager@example.com";

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));
        doNothing().when(permissionValidator)
                .validateAccess(email, projectId, ProjectMember.Role.MANAGER);
        when(userRepository.findByEmail(request.assigneeEmail()))
                .thenReturn(Optional.of(assignee));
        when(labelRepository.findAllById(request.labelIds()))
                .thenReturn(List.of(label1, label2));
        when(taskRepository.save(any())).thenReturn(savedTask);
        doNothing().when(emailService).sendNewTaskAssigned(assignee, savedTask);
        when(taskMapper.toDto(savedTask)).thenReturn(expectedDto);

        // when
        TaskResponseDto actual = taskServiceImpl.createTask(request, email);

        // then
        assertThat(actual).isEqualTo(expectedDto);

        verify(projectRepository,times(1)).findById(projectId);
        verify(permissionValidator,times(1))
                .validateAccess(email, projectId, ProjectMember.Role.MANAGER);

        verify(userRepository,times(1)).findByEmail(request.assigneeEmail());
        verify(taskMapper,times(1)).toModel(request);
        verify(labelRepository,times(1)).findAllById(request.labelIds());
        verify(taskRepository,times(1)).save(any());
        verify(emailService,times(1)).sendNewTaskAssigned(assignee, savedTask);
    }

    @Test
    @DisplayName("""
            createTask | verify that method throw EntityNotFoundException when project doesn't exist
            """)
    void createTask_projectNotFound_throwsException() {
        // given
        Long projectId = 1L;
        String requesterEmail = "manager@example.com";

        TaskRequestDto request = new TaskRequestDto(
                "Task name",
                "desc",
                Task.Priority.MEDIUM,
                Task.Status.IN_PROGRESS,
                LocalDate.now(),
                projectId,
                "john@example.com",
                Set.of(1L, 2L)
        );

        when(projectRepository.findById(request.projectId()))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->
                taskServiceImpl.createTask(request, requesterEmail)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository,times(1)).findById(projectId);
        verifyNoInteractions(permissionValidator, userRepository, labelRepository);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            createTask | verify that method throw EntityNotFoundException
             when assignee doesn't exist'
            """)
    void createTask_assigneeNotFound_throwsException() {
        // given
        Long projectId = 1L;
        String requesterEmail = "manager@example.com";
        String assigneeEmail = "missing@example.com";

        Project project = new Project();
        project.setId(projectId);

        TaskRequestDto request = new TaskRequestDto(
                "Task name",
                "desc",
                Task.Priority.HIGH,
                Task.Status.IN_PROGRESS,
                LocalDate.now(),
                projectId,
                assigneeEmail,
                Set.of(1L)
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator)
                .validateAccess(requesterEmail, projectId, ProjectMember.Role.MANAGER);
        when(userRepository.findByEmail(assigneeEmail))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->
                taskServiceImpl.createTask(request, requesterEmail)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository).findById(projectId);
        verify(permissionValidator).validateAccess(
                requesterEmail,
                projectId,
                ProjectMember.Role.MANAGER
        );
        verify(userRepository).findByEmail(assigneeEmail);
        verifyNoInteractions(labelRepository);
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            createTask |  verify method throw EntityNotFoundException
             when one or more labels missing
            """)
    void createTask_labelsMissing_throwsException() {
        // given
        Long projectId = 1L;

        Project project = new Project();
        project.setId(projectId);

        User assignee = new User();
        assignee.setEmail("john@email.com");

        Label onlyOne = new Label();
        onlyOne.setId(1L);

        String requesterEmail = "manager@example.com";

        TaskRequestDto request = new TaskRequestDto(
                "Task name",
                "desc",
                Task.Priority.LOW,
                Task.Status.IN_PROGRESS,
                LocalDate.now(),
                projectId,
                assignee.getEmail(),
                Set.of(1L, 2L)
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator)
                .validateAccess(anyString(), eq(projectId), eq(ProjectMember.Role.MANAGER));
        when(userRepository.findByEmail(assignee.getEmail()))
                .thenReturn(Optional.of(assignee));
        when(labelRepository.findAllById(request.labelIds()))
                .thenReturn(List.of(onlyOne));

        Task modelTask = new Task();
        when(taskMapper.toModel(request)).thenReturn(modelTask);

        // when + then
        assertThatThrownBy(() ->
                taskServiceImpl.createTask(request, requesterEmail)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(labelRepository,times(1)).findAllById(request.labelIds());
        verify(taskRepository, never()).save(any());
        verify(emailService, never()).sendNewTaskAssigned(any(), any());
    }

    @Test
    @DisplayName("""
            getTasksByProject | project exists, permission granted, returns mapped list
            """)
    void getTasksByProject_validRequest_success() {
        // given
        Long projectId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setName("Test Project");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Task 2");

        TaskResponseDto dto1 = new TaskResponseDto(
                task1.getId(),
                task1.getName(),
                null,
                null,
                null,
                null,
                project.getName(),
                null,
                Set.of()
        );

        TaskResponseDto dto2 = new TaskResponseDto(
                task2.getId(),
                task2.getName(),
                null,
                null,
                null,
                null,
                project.getName(),
                null,
                Set.of()
        );

        String email = "viewer@example.com";
        Page<Task> page = new PageImpl<>(List.of(task1, task2));
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator)
                .validateAccess(email, projectId, ProjectMember.Role.VIEWER);
        when(taskRepository.findByProject_Id(projectId,pageable))
                .thenReturn(page);
        when(taskMapper.toDto(task1)).thenReturn(dto1);
        when(taskMapper.toDto(task2)).thenReturn(dto2);

        // when
        List<TaskResponseDto> actual =
                taskServiceImpl.getTasksByProject(projectId, email, pageable);

        // then
        assertThat(actual).containsExactly(dto1, dto2);

        verify(projectRepository,times(1)).findById(projectId);
        verify(permissionValidator,times(1))
                .validateAccess(email, projectId, ProjectMember.Role.VIEWER);
        verify(taskRepository,times(1)).findByProject_Id(projectId,pageable);
    }

    @Test
    @DisplayName("""
            getTasksByProject | verify that method throw EntityNotFoundException
             when project not found
            """)
    void getTasksByProject_projectNotFound_throwsException() {
        // given
        Long projectId = 1L;
        String email = "viewer@example.com";
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->
                taskServiceImpl.getTasksByProject(projectId, email, Pageable.unpaged())
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository).findById(projectId);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(taskRepository);
        verify(taskMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("""
            getTaskById | valid request, return dto
            """)
    void getTaskById_valid_success() {
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);

        TaskResponseDto expected = new TaskResponseDto(
                taskId,
                "Test Task",
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of()
        );

        String email = "viewer@example.com";

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));
        doNothing()
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        when(taskMapper.toDto(task)).thenReturn(expected);

        TaskResponseDto actual = taskServiceImpl.getTaskById(taskId, email);

        assertThat(actual).isEqualTo(expected);

        verify(taskRepository,times(1)).findByIdWithRelations(taskId);
        verify(permissionValidator,times(1))
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
    }

    @Test
    @DisplayName("""
            getTaskById | validate that method throw EntityNotFoundException
             when task not found
            """)
    void getTaskById_taskNotFound_exception() {
        Long taskId = 1L;
        String email = "john@example.com";

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskServiceImpl.getTaskById(taskId, email))
                .isInstanceOf(EntityNotFoundException.class);

        verify(taskRepository).findByIdWithRelations(taskId);
        verifyNoMoreInteractions(permissionValidator, taskMapper);
    }

    @Test
    @DisplayName("""
            updateTask | validate that manager can update task.
             task exists, permission granted, returns Dto
            """)
    void updateTask_managerSuccess() {
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);
        project.setName("Old name");

        Task task = new Task();
        task.setId(taskId);
        task.setName("Old task name");
        task.setProject(project);

        Task updated = new Task();
        updated.setId(taskId);
        updated.setName("Updated name");
        updated.setDescription("Updated description");

        TaskResponseDto expected = new TaskResponseDto(
                taskId,
                "Updated name",
                "Updated description",
                null,
                null,
                null,
                project.getName(),
                null,
                Set.of());

        String email = "manager@mail.com";

        TaskPatchRequestDto requestPatchDto = new TaskPatchRequestDto(
                "Updated name",
                "Updated description",
                null,
                null,
                null,
                null,
                null
        );

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));

        doNothing()
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        when(taskRepository.save(task)).thenReturn(updated);
        when(taskMapper.toDto(updated)).thenReturn(expected);

        TaskResponseDto actual = taskServiceImpl.updateTask(taskId, requestPatchDto, email);

        assertThat(actual).isEqualTo(expected);

        verify(taskRepository).findByIdWithRelations(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(taskMapper).updateFromPatch(requestPatchDto, task);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("""
            updateTask | assignee updates only status.
             task exists, permission granted, returns Dto
            """)
    void updateTask_assigneeUpdatesOnlyStatus_success() {
        //given
        String email = "assignee@mail.com";

        Project project = new Project();
        project.setId(1L);
        project.setName("Project name");

        User assignee = new User();
        assignee.setEmail(email);
        assignee.setUsername("assignee");

        Long taskId = 1L;

        Task task = new Task();
        task.setId(taskId);
        task.setName("Task name");
        task.setProject(project);
        task.setAssignee(assignee);

        Task updated = new Task();
        updated.setId(taskId);

        TaskResponseDto expected = new TaskResponseDto(
                taskId,
                "Task name",
                null,
                null,
                Task.Status.IN_PROGRESS,
                null,
                project.getName(),
                task.getAssignee().getUsernameField(),
                Set.of()
        );

        TaskPatchRequestDto requestPatchDto = new TaskPatchRequestDto(
                null,
                null,
                null,
                Task.Status.IN_PROGRESS,
                null,
                null,
                null
        );

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));
        doThrow(new AccessDeniedException("no manager"))
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(assignee));
        when(taskRepository.save(task)).thenReturn(updated);
        when(taskMapper.toDto(updated)).thenReturn(expected);

        TaskResponseDto actual = taskServiceImpl.updateTask(taskId, requestPatchDto, email);

        assertThat(actual).isEqualTo(expected);

        verify(taskRepository).findByIdWithRelations(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(userRepository).findByEmail(email);
        verify(taskMapper).updateFromPatch(requestPatchDto, task);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("""
            updateTask | verify that method throw AccessDeniedException
            when assignee modifies forbidden fields
            """)
    void updateTask_assigneeModifiesForbiddenField_exception() {
        String email = "assignee@mail.com";

        Project project = new Project();
        project.setId(1L);

        User assignee = new User();
        assignee.setEmail(email);

        Long taskId = 1L;

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setAssignee(assignee);

        TaskPatchRequestDto requestPatchDto = new TaskPatchRequestDto(
                "new Name",
                null,
                null,
                Task.Status.IN_PROGRESS,
                null,
                null,
                null
        );

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));

        // manager check fails
        doThrow(new AccessDeniedException("no manager"))
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(assignee));

        assertThatThrownBy(() -> taskServiceImpl.updateTask(taskId, requestPatchDto, email))
                .isInstanceOf(AccessDeniedException.class);
        verify(taskRepository).findByIdWithRelations(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(taskMapper, taskRepository, labelRepository);
    }

    @Test
    @DisplayName("""
            updateTask | verify that method throws EntityNotFoundException
            when assignee user is not found
            """)
    void updateTask_assigneeNotFound_exception() {
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setProject(project);

        String email = "assignee@example.com";

        TaskPatchRequestDto requestPatchDto = new TaskPatchRequestDto(
                "new Name",
                null,
                null,
                Task.Status.IN_PROGRESS,
                null,
                null,
                null
        );

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));

        doThrow(new AccessDeniedException("no manager"))
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskServiceImpl.updateTask(taskId, requestPatchDto, email))
                .isInstanceOf(EntityNotFoundException.class);

        verify(taskRepository).findByIdWithRelations(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("""
            updateTask | verify that method throws AccessDeniedException
            when user exists but is not assigned to task
            """)
    void updateTask_userNotAssignee_exception() {

        Project project = new Project();
        project.setId(1L);

        User assignee = new User();
        assignee.setEmail("assignee@example.com");

        Long taskId = 1L;

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setAssignee(assignee);

        String email = "john@example.com";

        User requester = new User();
        requester.setEmail(email);

        TaskPatchRequestDto requestPatchDto = new TaskPatchRequestDto(
                "new Name",
                null,
                null,
                Task.Status.IN_PROGRESS,
                null,
                null,
                null
        );

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));

        doThrow(new AccessDeniedException("no manager"))
                .when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> taskServiceImpl.updateTask(taskId, requestPatchDto, email))
                .isInstanceOf(AccessDeniedException.class);

        verify(taskRepository).findByIdWithRelations(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("""
            updateTask | verify that method throw EntityNotFoundException
             when some labels are missing
            """)
    void updateTask_shouldThrowEntityNotFound_whenSomeLabelsMissing() {
        // given
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);

        TaskPatchRequestDto request = new TaskPatchRequestDto(
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of(1L, 2L)
        );

        Label existingLabel = new Label();
        existingLabel.setId(1L);

        String email = "manager@test.com";

        when(taskRepository.findByIdWithRelations(taskId))
                .thenReturn(Optional.of(task));
        when(labelRepository.findAllById(request.labelIds()))
                .thenReturn(List.of(existingLabel));

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> taskServiceImpl.updateTask(taskId, request, email));
        verify(taskRepository).findByIdWithRelations(taskId);
        verify(labelRepository).findAllById(request.labelIds());
        verify(taskMapper, never()).updateFromPatch(any(), any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("""
            deleteTask | verify that method delete task when User has access
            """)
    void deleteTask_shouldDeleteTask_whenManagerHasAccess() {
        // given
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);

        String email = "manager@example.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);

        // when
        taskServiceImpl.deleteTask(taskId, email);

        // then
        verify(taskRepository).findById(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("""
            deleteTask | should throw EntityNotFoundException
             when task does not exist
            """)
    void deleteTask_shouldThrowNotFound_whenTaskNotExists() {
        // given
        Long taskId = 1L;
        String email = "manager@example.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> taskServiceImpl.deleteTask(taskId, email));

        // verify
        verify(taskRepository).findById(taskId);
        verify(permissionValidator, never()).validateAccess(any(), any(), any());
        verify(taskRepository, never()).delete(any());
    }

}


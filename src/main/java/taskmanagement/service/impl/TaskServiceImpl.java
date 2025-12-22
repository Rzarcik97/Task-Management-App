package taskmanagement.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import taskmanagement.dto.task.TaskPatchRequestDto;
import taskmanagement.dto.task.TaskRequestDto;
import taskmanagement.dto.task.TaskResponseDto;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.TaskMapper;
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
import taskmanagement.service.EmailService;
import taskmanagement.service.TaskService;

@Log4j2
@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final PermissionValidator permissionValidator;
    private final LabelRepository labelRepository;
    private final EmailService emailService;

    @Override
    public TaskResponseDto createTask(TaskRequestDto request, String email) {
        log.info("Starting creating task: name = {}", request.name());
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id " + request.projectId() + " not found"));
        permissionValidator.validateAccess(email,
                project.getId(),
                ProjectMember.Role.MANAGER);
        User assignee = userRepository.findByEmail(request.assigneeEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email " + request.assigneeEmail() + " not found"));
        Task task = taskMapper.toModel(request);
        task.setName(task.getName());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setPriority(request.priority());
        task.setStatus(request.status());
        List<Label> labels = labelRepository.findAllById(request.labelIds());
        if (labels.size() != request.labelIds().size()) {
            throw new EntityNotFoundException("One or more labels not found");
        }
        task.getLabels().addAll(labels);
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully: id = {}", savedTask.getId());
        emailService.sendNewTaskAssigned(assignee, savedTask);
        return taskMapper.toDto(savedTask);
    }

    @Override
    public List<TaskResponseDto> getTasksByProject(Long projectId,
                                                   String email,
                                                   Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id " + projectId + " not found"));
        permissionValidator.validateAccess(email,
                project.getId(),
                ProjectMember.Role.VIEWER);
        Page<Task> tasks = taskRepository.findByProject_Id(projectId,pageable);
        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    public TaskResponseDto getTaskById(Long taskId, String email) {
        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + taskId + " not found"));
        Long projectId = task.getProject().getId();
        permissionValidator.validateAccess(email, projectId, ProjectMember.Role.VIEWER);
        return taskMapper.toDto(task);
    }

    @Override
    public TaskResponseDto updateTask(Long taskId, TaskPatchRequestDto request, String email) {
        log.info("Starting editing task: id = {}", taskId);
        Task task = taskRepository.findByIdWithRelations(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + taskId + " not found"));
        if (request.labelIds() != null) {
            task.getLabels().addAll(labelRepository.findAllById(request.labelIds()));
            if (task.getLabels().size() != request.labelIds().size()) {
                throw new EntityNotFoundException("One or more labels not found");
            }
        }
        Long projectId = task.getProject().getId();
        try {
            permissionValidator.validateAccess(email, projectId, ProjectMember.Role.MANAGER);
        } catch (AccessDeniedException e) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User with email: " + email + " not found"));
            if (!task.getAssignee().equals(user)) {
                throw new AccessDeniedException("You don't have permission to update this task");
            }

            if (!(request.name() == null
                    && request.description() == null
                    && request.dueDate() == null
                    && request.assigneeEmail() == null
                    && request.priority() == null)) {
                throw new AccessDeniedException(
                        "Assignee can update only the task status and Labels");
            }
        }
        taskMapper.updateFromPatch(request, task);
        Task updatedTask = taskRepository.save(task);
        log.info("Task edited successfully");
        return taskMapper.toDto(updatedTask);
    }

    @Override
    public void deleteTask(Long taskId, String email) {
        log.info("Starting deleting task: id = {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + taskId + " not found"));
        Long projectId = task.getProject().getId();
        permissionValidator.validateAccess(email,
                projectId,
                ProjectMember.Role.MANAGER);
        taskRepository.delete(task);
        log.info("Task deleted successfully");
    }
}

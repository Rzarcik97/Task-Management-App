package taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.task.TaskPatchRequestDto;
import taskmanagement.dto.task.TaskRequestDto;
import taskmanagement.dto.task.TaskResponseDto;
import taskmanagement.service.TaskService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Task",
            description = "Create a new task inside a project (only MANAGER can create tasks)")
    public TaskResponseDto createTask(@RequestBody @Valid TaskRequestDto request,
                                        Authentication authentication) {
        String email = authentication.getName();
        log.info("Creating Task {}, by User {}", request.name(), email);
        return taskService.createTask(request,email);
    }

    @GetMapping("/by-project/{projectId}")
    @Operation(summary = "Get Project Tasks",
            description = "Retrieve all tasks for a given project "
                    + "(projectId required as request param)")
    public List<TaskResponseDto> getTasksByProject(@PathVariable Long projectId,
                                                   Authentication authentication) {
        String email = authentication.getName();
        return taskService.getTasksByProject(projectId,email);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get Task Details",
            description = "Retrieve details of a specific task by ID")
    public TaskResponseDto getTaskById(@PathVariable Long taskId,
                                       Authentication authentication) {
        String email = authentication.getName();
        return taskService.getTaskById(taskId,email);
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "Update Task",
            description = "Update details of an existing task (USER assigned to task can update)")
    public TaskResponseDto updateTask(@PathVariable Long taskId,
                                      @RequestBody @Valid TaskPatchRequestDto request,
                                      Authentication authentication) {
        String email = authentication.getName();
        log.info("Editing Task {}, by User {}", taskId, email);
        return taskService.updateTask(taskId, request, email);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete Task",
            description = "Delete a task by ID (only MANAGER can delete tasks)")
    public void deleteTask(@PathVariable Long taskId,
                           Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting Task {}, by User {}", taskId, email);
        taskService.deleteTask(taskId, email);
    }
}

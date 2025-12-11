package taskmanagement.service;

import java.util.List;
import taskmanagement.dto.task.TaskPatchRequestDto;
import taskmanagement.dto.task.TaskRequestDto;
import taskmanagement.dto.task.TaskResponseDto;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto request, String email);

    List<TaskResponseDto> getTasksByProject(Long projectId, String email);

    TaskResponseDto getTaskById(Long id, String email);

    TaskResponseDto updateTask(Long id, TaskPatchRequestDto request, String email);

    void deleteTask(Long id, String email);
}

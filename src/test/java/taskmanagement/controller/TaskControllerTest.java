package taskmanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import taskmanagement.dto.task.TaskPatchRequestDto;
import taskmanagement.dto.task.TaskRequestDto;
import taskmanagement.model.Task;
import taskmanagement.service.EmailService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class TaskControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(ctx)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Create Task - as Admin – should return 201")
    void createTask_asAdmin_success() throws Exception {

        TaskRequestDto request = new TaskRequestDto(
                "New Task2",
                "Task description2",
                Task.Priority.LOW,
                Task.Status.IN_PROGRESS,
                LocalDate.of(2004, 1, 1),
                1L,
                "john.doe@example.com",
                Set.of(1L)
        );

        doNothing().when(emailService).sendNewTaskAssigned(any(),any());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Task2"))
                .andExpect(jsonPath("$.description").value("Task description2"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assigneeEmail").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Create Task - as Manager – should return 201")
    void createTask_asManager_success() throws Exception {

        TaskRequestDto request = new TaskRequestDto(
                "New Task",
                "Task description",
                Task.Priority.LOW,
                Task.Status.IN_PROGRESS,
                LocalDate.of(2004, 1, 1),
                2L,
                "jane.smith@example.com",
                Set.of(1L)
        );

        doNothing().when(emailService).sendNewTaskAssigned(any(),any());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assigneeEmail").value("jane.smith@example.com"));
    }

    @Test
    @WithMockUser(username = "jane.smith@example.com")
    @DisplayName("Create Task - as User – return 403")
    void createTask_asUser_success() throws Exception {

        TaskRequestDto request = new TaskRequestDto(
                "New Task3",
                "Task description3",
                Task.Priority.LOW,
                Task.Status.IN_PROGRESS,
                LocalDate.of(2004, 1, 1),
                2L,
                "john.doe@example.com",
                Set.of(1L)
        );

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Create Task - project is not found – return 404")
    void createTask_missingProjectInDB_success() throws Exception {

        TaskRequestDto request = new TaskRequestDto(
                "New Task2",
                "Task description2",
                Task.Priority.LOW,
                Task.Status.IN_PROGRESS,
                LocalDate.of(2004, 1, 1),
                999L,
                "john.doe@example.com",
                Set.of(1L)
        );

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get Tasks by Project – success")
    void getTasksByProject_accessGranted_success() throws Exception {

        mockMvc.perform(get("/tasks")
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "jane.smith@example.com")
    @DisplayName("Get Tasks by Project - User is not member of project – return 403")
    void getTasksByProject_accessDenied_success() throws Exception {

        mockMvc.perform(get("/tasks")
                        .param("projectId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get Tasks by Project – success")
    void getTasksByProject_missingProjectInDB_notFound() throws Exception {

        mockMvc.perform(get("/tasks")
                        .param("projectId", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get Task By ID - asMember – success")
    void getTaskById_asMember_success() throws Exception {

        mockMvc.perform(get("/tasks/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @WithMockUser(username = "jane.smith@example.com")
    @DisplayName("Get Task By ID - not Member of project – return 403")
    void getTaskById_notMemberOfProject_success() throws Exception {

        mockMvc.perform(get("/tasks/{id}", 2L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get Task By ID - task not found – return 404")
    void getTaskById_missingTaskInDB_notFound() throws Exception {

        mockMvc.perform(get("/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update Task – as assignee - success")
    void updateTask_success() throws Exception {

        TaskPatchRequestDto request = new TaskPatchRequestDto(
                null,
                null,
                null,
                Task.Status.COMPLETED,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/tasks/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update Task – as assignee - return 403")
    void updateTask_asAssigneeForbiddenAction_forbidden() throws Exception {

        TaskPatchRequestDto request = new TaskPatchRequestDto(
                null,
                "new Description",
                null,
                Task.Status.COMPLETED,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/tasks/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Update Task – as Manager - success")
    void updateTask_asManager_success() throws Exception {

        TaskPatchRequestDto request = new TaskPatchRequestDto(
                "new name",
                "new Description",
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/tasks/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new name"))
                .andExpect(jsonPath("$.description").value("new Description"));
    }

    @Test
    @WithMockUser(username = "jane.smith@example.com")
    @DisplayName("Update Task – as User - return 403")
    void updateTask_asUser_success() throws Exception {

        TaskPatchRequestDto request = new TaskPatchRequestDto(
                "new name",
                "new Description",
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/tasks/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Update Task – task is not found - return 404")
    void updateTask_missingTaskInDB_success() throws Exception {

        TaskPatchRequestDto request = new TaskPatchRequestDto(
                null,
                null,
                null,
                Task.Status.COMPLETED,
                null,
                null,
                null
        );

        mockMvc.perform(patch("/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Delete Task – as manager – success")
    void deleteTask_asManager_success() throws Exception {

        mockMvc.perform(delete("/tasks/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "jane.smith@example.com")
    @DisplayName("Delete Task – as User – return 403")
    void deleteTask_asUser_forbidden() throws Exception {

        mockMvc.perform(delete("/tasks/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Delete Task – task is not found – return 404")
    void deleteTask_missingTaskInDB_notFound() throws Exception {

        mockMvc.perform(delete("/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}

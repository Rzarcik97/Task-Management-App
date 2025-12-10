package taskmanagement.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import taskmanagement.dto.project.ProjectMemberRequest;
import taskmanagement.dto.project.ProjectPatchRequestDto;
import taskmanagement.dto.project.ProjectRequestDto;
import taskmanagement.dto.projectmember.ProjectMemberDto;
import taskmanagement.model.ProjectMember;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class ProjectControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void setUp(@Autowired WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(ctx)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com", roles = "ADMIN")
    @DisplayName("Create Project - create as ADMIN, add new Manager - success")
    void createProject_addNewManager_success() throws Exception {

        ProjectRequestDto request = new ProjectRequestDto(
                "Test Project",
                "Some description",
                null);

        ProjectMemberDto memberResponse = new ProjectMemberDto(
                "anna@example.com",
                ProjectMember.Role.MANAGER.toString());

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("projectManagerEmail", "anna@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Some description"))
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].username").value("anna@example.com"))
                .andExpect(jsonPath("$.members[0].role").value("MANAGER"));
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com", roles = "ADMIN")
    @DisplayName("Create Project - create as ADMIN - success")
    void createProject_defaultManager_success() throws Exception {

        ProjectRequestDto request = new ProjectRequestDto(
                "Test Project",
                "Some description",
                null);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Some description"))
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].username").value("admin@taskmanager.com"))
                .andExpect(jsonPath("$.members[0].role").value("MANAGER"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Create Project - create as User - should return 403")
    void createProject_asUser_forbidden() throws Exception {

        ProjectRequestDto request = new ProjectRequestDto(
                "Test Project",
                "Some description",
                null);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Create Project - invalid request - fail validation")
    void createProject_invalidRequest() throws Exception {

        ProjectRequestDto request = new ProjectRequestDto(
                "Test Project",
                null,
                null);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get User Projects - return user project list")
    void getUserProjects_success() throws Exception {

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Get Project by id - return project")
    void getProjectById_success() throws Exception {

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Add Member - asAdmin -  add member successfully")
    void addMember_asAdmin_success() throws Exception {

        ProjectMemberRequest request = new ProjectMemberRequest(
                "anna@example.com",
                ProjectMember.Role.MEMBER);

        mockMvc.perform(post("/projects/1/Member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("anna@example.com"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Add Member - asManager - add member successfully")
    void addMember_asManager_success() throws Exception {

        ProjectMemberRequest request = new ProjectMemberRequest(
                "jane.smith@example.com",
                ProjectMember.Role.MEMBER);

        mockMvc.perform(post("/projects/2/Member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Add Member - asMember - should return 403")
    void addMember_forbidden() throws Exception {

        ProjectMemberRequest request = new ProjectMemberRequest(
                "anna@example.com",
                ProjectMember.Role.MEMBER);

        mockMvc.perform(post("/projects/1/Member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Add Member - invalid request - fail validation")
    void addMember_invalid() throws Exception {

        ProjectMemberRequest request = new ProjectMemberRequest(
                "",
                ProjectMember.Role.MEMBER);

        mockMvc.perform(post("/projects/1/Member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Add Member - missingEmail in DB - return 404")
    void addMember_missingEmail_invalid() throws Exception {

        ProjectMemberRequest request = new ProjectMemberRequest(
                "nonexisting@example.com",
                ProjectMember.Role.MEMBER);

        mockMvc.perform(post("/projects/1/Member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Delete Member - asAdmin - delete member successfully")
    void deleteMember_asAdmin_success() throws Exception {

        mockMvc.perform(delete("/projects/1/Member")
                        .param("memberEmail", "john.doe@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Member - asManager - delete member successfully")
    void deleteMember_asManager_success() throws Exception {

        mockMvc.perform(delete("/projects/2/Member")
                        .param("memberEmail", "jane.smith@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Member - asMember - return 403")
    void deleteMember_forbidden() throws Exception {

        mockMvc.perform(delete("/projects/1/Member")
                        .param("memberEmail", "admin@taskmanager.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com", roles = {"ADMIN"})
    @DisplayName("Delete Member - invalid request - return 400")
    void deleteMember_missingEmailInRequest() throws Exception {

        mockMvc.perform(delete("/projects/1/Member"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Member - non existing email in DB - return 404")
    void deleteMember_missingEmailInDB() throws Exception {

        mockMvc.perform(delete("/projects/1/Member")
                        .param("memberEmail", "nonexisting@taskmanager.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Update Project - asAdmin - success")
    void updateProject_asAdmin_success() throws Exception {

        ProjectPatchRequestDto dto = new ProjectPatchRequestDto(
                "Updated name",
                "Updated desc",
                null,
                null,
                null);

        mockMvc.perform(patch("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update Project - asManager - success")
    void updateProject_asManager_success() throws Exception {

        ProjectPatchRequestDto dto = new ProjectPatchRequestDto(
                "Updated name",
                "Updated desc",
                null,
                null,
                null);

        mockMvc.perform(patch("/projects/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated name"))
                .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update Project - asUser -return 403")
    void updateProject_forbidden() throws Exception {

        ProjectPatchRequestDto dto = new ProjectPatchRequestDto(
                "Updated name",
                "Updated desc",
                null,
                null,
                null);

        mockMvc.perform(patch("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Update Project - not existing project - return 404")
    void updateProject_notExistingProject_notFound() throws Exception {

        ProjectPatchRequestDto dto = new ProjectPatchRequestDto(
                "Updated name",
                "Updated desc",
                null,
                null,
                null);

        mockMvc.perform(patch("/projects/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Delete Project - asAdmin - success")
    void deleteProject_asAdmin_success() throws Exception {

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Project - asManager - success")
    void deleteProject_asManager_success() throws Exception {

        mockMvc.perform(delete("/projects/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Project - asMember - return 403")
    void deleteProject_forbidden() throws Exception {

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Project - return 404")
    void deleteProject_nonExistingProject_notFound() throws Exception {

        mockMvc.perform(delete("/projects/999"))
                .andExpect(status().isNotFound());
    }

}

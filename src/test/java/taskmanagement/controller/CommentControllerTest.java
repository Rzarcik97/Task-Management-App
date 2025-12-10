package taskmanagement.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import taskmanagement.dto.comment.CommentRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class CommentControllerTest {

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
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Add comment - valid request - should return 200")
    void addComment_ValidRequest_ReturnsOk() throws Exception {
        CommentRequestDto request = new CommentRequestDto(
                1L,
                "This is a new test comment"
        );

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("This is a new test comment"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Add comment - missing text - should return 400")
    void addComment_MissingText_ReturnsBadRequest() throws Exception {
        CommentRequestDto request = new CommentRequestDto(1L, null);

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Add comment - task does not exist - should return 404")
    void addComment_TaskNotFound_ReturnsNotFound() throws Exception {
        CommentRequestDto request = new CommentRequestDto(
                9999L,
                "Hello"
        );

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get comments - valid task - should return list")
    void getComments_ValidTask_ReturnsList() throws Exception {

        mockMvc.perform(get("/comments")
                        .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get comments - task does not exist - returns empty list")
    void getComments_TaskNotFound_ReturnsEmptyList() throws Exception {

        mockMvc.perform(get("/comments")
                        .param("taskId", "9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update comment - user owns comment - should return updated dto")
    void updateComment_Owner_ReturnsOk() throws Exception {

        mockMvc.perform(put("/comments/2")
                        .param("text", "Updated text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update comment - user does NOT own comment - should return 403")
    void updateComment_NotOwner_ReturnsForbidden() throws Exception {

        mockMvc.perform(put("/comments/1")
                        .param("text", "Trying to update"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update comment - comment not found - 404")
    void updateComment_NotFound_ReturnsNotFound() throws Exception {

        mockMvc.perform(put("/comments/9999")
                        .param("text", "anything"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete comment - user owns comment - should return 200")
    void deleteComment_Owner_ReturnsOk() throws Exception {

        mockMvc.perform(delete("/comments/2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete comment - NOT owner - should return 403")
    void deleteComment_NotOwner_ReturnsForbidden() throws Exception {

        mockMvc.perform(delete("/comments/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete comment - comment not found - 404")
    void deleteComment_NotFound_ReturnsNotFound() throws Exception {

        mockMvc.perform(delete("/comments/9999"))
                .andExpect(status().isNotFound());
    }
}

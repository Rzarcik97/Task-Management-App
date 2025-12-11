package taskmanagement.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import taskmanagement.dto.label.LabelPatchRequestDto;
import taskmanagement.dto.label.LabelRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class LabelControllerTest {

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
    @WithMockUser(username = "admin@taskmanager.com",roles = "ADMIN")
    @DisplayName("Create Label - access granted - success")
    void createLabel_ShouldCreate_WhenAdmin() throws Exception {
        LabelRequestDto createRequest = new LabelRequestDto("Test Label", "Green");

        mockMvc.perform(MockMvcRequestBuilders.post("/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.name").value("Test Label"))
                .andExpect(jsonPath("$.color").value("Green"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Create Label - access denied - should return 403")
    void createLabel_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        LabelRequestDto createRequest = new LabelRequestDto(
                "name",
                "color");
        mockMvc.perform(MockMvcRequestBuilders.post("/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com",roles = "ADMIN")
    @DisplayName("Create Label - validation fail - should return 400")
    void createLabel_ShouldFailValidation_WhenNameBlank() throws Exception {

        LabelRequestDto invalid = new LabelRequestDto("", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser("john.doe@example.com")
    @DisplayName("Get Labels - should return list of labels")
    void getLabels_ShouldReturnList() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com",roles = "ADMIN")
    @DisplayName("Update Label - access granted - success")
    void updateLabel_ShouldUpdate_WhenAdmin() throws Exception {

        LabelPatchRequestDto patch = new LabelPatchRequestDto("Updated",null);

        mockMvc.perform(MockMvcRequestBuilders.patch("/labels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update Label - access denied - should return 403")
    void updateLabel_ShouldReturnForbidden_WhenNotAdmin() throws Exception {

        LabelPatchRequestDto patch = new LabelPatchRequestDto("test","Green");

        mockMvc.perform(MockMvcRequestBuilders.patch("/labels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com",roles = "ADMIN")
    @DisplayName("Delete Label - access granted - success")
    void deleteLabel_ShouldDelete_WhenAdmin() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/labels/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Delete Label - access denied - should return 403")
    void deleteLabel_ShouldReturnForbidden_WhenNotAdmin() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/labels/1"))
                .andExpect(status().isForbidden());
    }
}

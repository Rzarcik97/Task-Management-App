package taskmanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import taskmanagement.dto.user.UserPatchRequestDto;
import taskmanagement.service.EmailService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class UserControllerTest {

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
    @WithMockUser(username = "admin@taskmanager.com", roles = {"ADMIN"})
    @DisplayName("Update User Role - as admin - success")
    void updateRole_success() throws Exception {

        mockMvc.perform(put("/users/2/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update User Role - as user - forbidden")
    void updateRole_forbiddenForUser() throws Exception {
        mockMvc.perform(put("/users/2/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com", roles = {"ADMIN"})
    @DisplayName("Update User Role - missing user in DB- user not found")
    void updateRole_userNotFound() throws Exception {
        mockMvc.perform(put("/users/9999/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Get My Profile - success")
    void getMyProfile_success() throws Exception {

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "notexisting@example.com")
    @DisplayName("Get My Profile - user does not exist in DB")
    void getMyProfile_userNotFound() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Update My Profile - success")
    void updateMyProfile_success() throws Exception {

        UserPatchRequestDto dto = new UserPatchRequestDto(
                null,
                "John",
                "Newman"
        );

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Newman"));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Change Email - success")
    void changeEmail_success() throws Exception {

        doNothing().when(emailService).sendEmailChangeVerification(any(),any(),any());

        mockMvc.perform(patch("/users/me/change-email")
                        .param("newEmail", "updated@mail.com")
                        .param("currentPassword", "User123"))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Change Email - invalid email format - return 400")
    void changeEmail_invalidEmailFormat_success() throws Exception {

        mockMvc.perform(patch("/users/me/change-email")
                        .param("newEmail", "testFormat")
                        .param("currentPassword", "password123"))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Change Email - wrong current password  - return 401")
    void changeEmail_wrongPassword() throws Exception {

        mockMvc.perform(patch("/users/me/change-email")
                        .param("newEmail", "new@mail.com")
                        .param("currentPassword", "WRONG"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Change Password - success")
    void changePassword_success() throws Exception {

        doNothing().when(emailService).sendPasswordChangeVerification(any(),any());

        mockMvc.perform(patch("/users/me/change-password")
                        .param("oldPassword", "User123")
                        .param("newPassword", "NewPassword123"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Change Password - new password too short - return 400")
    void changePassword_newPasswordTooShort() throws Exception {

        mockMvc.perform(patch("/users/me/change-password")
                        .param("oldPassword", "User123")
                        .param("newPassword", "a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@taskmanager.com")
    @DisplayName("Verify Change - passwordChange- success")
    void verifyChange_passwordChange_success() throws Exception {

        mockMvc.perform(post("/users/verify")
                        .param("request", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@taskmanager.com"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Verify Change - email change - success")
    void verifyChange_emailChange_success() throws Exception {

        mockMvc.perform(post("/users/verify")
                        .param("request", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @WithMockUser(username = "john.doe@example.com")
    @DisplayName("Verify Change - invalid verification code - return 401")
    void verifyChange_invalidCode() throws Exception {

        mockMvc.perform(post("/users/verify")
                        .param("request", "WRONG_CODE"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "missing@example.com")
    @DisplayName("Verify Change - user not found in DB - return 404")
    void verifyChange_userNotFound() throws Exception {

        mockMvc.perform(post("/users/verify")
                        .param("request", "123456"))
                .andExpect(status().isNotFound());
    }
}

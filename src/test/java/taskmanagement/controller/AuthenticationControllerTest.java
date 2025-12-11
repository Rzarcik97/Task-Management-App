package taskmanagement.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import taskmanagement.dto.user.UserLoginRequestDto;
import taskmanagement.dto.user.UserRegistrationRequestDto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class AuthenticationControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void setUp(
            @Autowired WebApplicationContext ctx
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(ctx)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Register - valid request - should return 201 and user dto")
    void register_ValidRequest_ReturnsCreated() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "newUser",
                "password123",
                "newuser@example.com",
                "New",
                "User"
        );

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Register - email already exists - should return 400")
    void register_EmailExists_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "newUser",
                "password123",
                "admin@taskmanager.com",
                "New",
                "User"
        );

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Register - invalid email - should return 400")
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "newUser",
                "password123",
                "bad-email-format",
                "New",
                "User"
        );

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register - missing password - should return 400")
    void register_MissingPassword_ReturnsBadRequest() throws Exception {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "newUser",
                null,
                "newUser@example.com",
                "New",
                "User"
        );

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login - valid credentials - should return 200 and JWT")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "admin@taskmanager.com",
                "Admin123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Login - wrong password - should return 401")
    void login_InvalidPassword_ReturnsUnauthorized() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "admin@taskmanager.com",
                "wrongPassword"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid login details"));
    }

    @Test
    @DisplayName("Login - user not found - should return 401")
    void login_UserNotFound_ReturnsUnauthorized() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "notexisting@example.com",
                "anyPassword"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid login details"));
    }

    @Test
    @DisplayName("Login - missing password - should return 400")
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "admin@example.com",
                null
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}


package taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.user.UserLoginRequestDto;
import taskmanagement.dto.user.UserLoginResponseDto;
import taskmanagement.dto.user.UserRegistrationRequestDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.service.AuthenticationService;

@Log4j2
@Tag(name = "Authentication_controller", description = "Authentication management controller")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @Operation(summary = "Register User", description = "Register new user in dataBase")
    UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request) {
        log.info("Register User {}", request.email());
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login User", description = "Authorize user with dataBase")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        log.info("Login User {}", request.email());
        return authenticationService.login(request);
    }
}

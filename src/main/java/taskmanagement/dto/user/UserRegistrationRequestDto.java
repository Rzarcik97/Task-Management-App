package taskmanagement.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequestDto(
        @NotBlank String username,
        @NotBlank @Size(min = 6) String password,
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName
)
{}


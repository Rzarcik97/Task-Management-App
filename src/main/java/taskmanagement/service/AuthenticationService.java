package taskmanagement.service;

import taskmanagement.dto.user.UserLoginRequestDto;
import taskmanagement.dto.user.UserLoginResponseDto;
import taskmanagement.dto.user.UserRegistrationRequestDto;
import taskmanagement.dto.user.UserResponseDto;

public interface AuthenticationService {

    UserResponseDto register(UserRegistrationRequestDto userRegistrationRequestDto);

    UserLoginResponseDto login(UserLoginRequestDto request);
}

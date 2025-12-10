package taskmanagement.service;

import taskmanagement.dto.user.UserPatchRequestDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.model.User;

public interface UserService {
    UserResponseDto getMyProfile(String email);

    UserResponseDto updateMyProfile(String email, UserPatchRequestDto request);

    UserResponseDto updateUserRole(Long userId, User.Role newRole);

    void changeEmail(String email, String newEmail, String currentPassword);

    void changePassword(String email, String oldPassword, String newPassword);

    UserResponseDto verifyChange(String email, String rawCode);
}

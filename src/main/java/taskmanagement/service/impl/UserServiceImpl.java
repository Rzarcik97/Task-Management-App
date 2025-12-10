package taskmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taskmanagement.dto.user.UserPatchRequestDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.exceptions.AuthenticationException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.exceptions.RegistrationException;
import taskmanagement.mapper.UserMapper;
import taskmanagement.model.User;
import taskmanagement.model.UserVerificationToken;
import taskmanagement.repository.UserRepository;
import taskmanagement.repository.VerificationTokenRepository;
import taskmanagement.security.VerificationTokenService;
import taskmanagement.service.EmailService;
import taskmanagement.service.UserService;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Override
    public UserResponseDto getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + email + " not found"));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateMyProfile(String email, UserPatchRequestDto request) {
        log.info("starting editing user {} profile",email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + email + " not found"));
        userMapper.updateFromPatch(request,user);
        User updatedUser = userRepository.save(user);
        log.info("User profile edited successfully");
        return userMapper.toDto(updatedUser);
    }

    @Override
    public UserResponseDto updateUserRole(Long userId, User.Role newRole) {
        log.info("starting editing user with id = {} role",userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id: " + userId + " not found"));
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        log.info("User role edited successfully");
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @Override
    public void changeEmail(String email, String newEmail, String currentPassword) {
        log.info("user {} requested change of email",email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + email + " not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid current password");
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RegistrationException("Email already exists");
        }
        String rawCode = verificationTokenService.createToken(user,
                UserVerificationToken.TokenType.EMAIL_CHANGE,
                newEmail);
        emailService.sendEmailChangeVerification(user, newEmail, rawCode);
    }

    @Transactional
    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        log.info("user {} requested change of password",email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + email + " not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid current password");
        }
        newPassword = passwordEncoder.encode(newPassword);
        String rawCode = verificationTokenService.createToken(user,
                UserVerificationToken.TokenType.PASSWORD_CHANGE,
                newPassword);
        emailService.sendPasswordChangeVerification(user, rawCode);
    }

    @Override
    public UserResponseDto verifyChange(String email, String rawCode) {
        log.info("user {} entered verification code",email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + email + " not found"));

        UserVerificationToken token = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException(
                        "verification code not found"));

        if (verificationTokenService.validateCode(user,rawCode,token.getType())) {
            log.info("verification completed successfully");
            if (token.getType().equals(UserVerificationToken.TokenType.EMAIL_CHANGE)) {
                user.setEmail(token.getNewValue());
                log.info("changing User's email");
            } else if (token.getType().equals(UserVerificationToken.TokenType.PASSWORD_CHANGE)) {
                user.setPassword(token.getNewValue());
                log.info("changing User's password");
            }
        } else {
            throw new AuthenticationException("Invalid verification code");
        }
        userRepository.save(user);
        log.info("deleting verification token");
        verificationTokenRepository.delete(token);
        log.info("token deleted successfully");
        log.info("change ended successfully");
        return userMapper.toDto(user);
    }
}

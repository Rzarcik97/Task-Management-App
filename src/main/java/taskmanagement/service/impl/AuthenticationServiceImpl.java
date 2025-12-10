package taskmanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import taskmanagement.dto.user.UserLoginRequestDto;
import taskmanagement.dto.user.UserLoginResponseDto;
import taskmanagement.dto.user.UserRegistrationRequestDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.exceptions.AuthenticationException;
import taskmanagement.exceptions.RegistrationException;
import taskmanagement.mapper.UserMapper;
import taskmanagement.model.User;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.JwtUtil;
import taskmanagement.service.AuthenticationService;

@Log4j2
@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto userRegistrationRequestDto) {
        log.info("starting register User {}", userRegistrationRequestDto.email());
        if (userRepository.findByEmail(userRegistrationRequestDto.email()).isPresent()) {
            throw new RegistrationException("Email already exists");
        }
        User user = userMapper.registerModelFromDto(userRegistrationRequestDto);
        user.setPassword(passwordEncoder.encode(userRegistrationRequestDto.password()));
        user.setEmail(user.getEmail().toLowerCase());
        user.setRole(User.Role.USER);
        User savedUser = userRepository.save(user);
        log.info("User register successfully: id = {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserLoginResponseDto login(UserLoginRequestDto request) {
        log.info("starting login User {}", request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            String token = jwtUtil.generateToken(authentication.getName());
            log.info("User logged successfully");
            return new UserLoginResponseDto(token);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("invalid login details");
        }
    }
}

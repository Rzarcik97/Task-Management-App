package taskmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import taskmanagement.service.impl.AuthenticationServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;
    
    @Test
    @DisplayName("""
            register | verify that method register user successfully
             with valid entry
            """)
    void register_success() {
        // given
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("encodedPass");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("john@example.com");

        UserResponseDto responseDto = new UserResponseDto(
                1L,
                "JohnD",
                "john@example.com",
                "John",
                "Doe",
                User.Role.USER.toString()
        );

        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "JohnD",
                "password",
                "john@example.com",
                "John",
                "Doe"
        );

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.registerModelFromDto(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(responseDto);

        // when
        UserResponseDto result = authenticationService.register(request);

        // then
        assertEquals(responseDto, result);
        verify(userRepository).findByEmail("john@example.com");
        verify(userMapper).registerModelFromDto(request);
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(user);
        verify(userMapper).toDto(savedUser);
    }

    @Test
    @DisplayName("""
            register | verify that method throw RegistrationException
            when email already exists
            """)
    void register_emailExists() {
        // given
        UserRegistrationRequestDto request = new UserRegistrationRequestDto(
                "JohnD",
                "password",
                "john@example.com",
                "John",
                "Doe"
        );
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        // when + then
        assertThrows(
                RegistrationException.class,
                () -> authenticationService.register(request)
        );

        verify(userRepository).findByEmail("john@example.com");
        verifyNoMoreInteractions(userMapper, passwordEncoder, jwtUtil, authenticationManager);
    }

    @Test
    @DisplayName("""
            login | should authenticate user and return token
            """)
    void login_success() {
        // given
        UserLoginRequestDto request = new UserLoginRequestDto(
                "john@example.com", "pass123"
        );

        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken(any())).thenReturn("token123");

        // when
        UserLoginResponseDto result = authenticationService.login(request);

        // then
        assertEquals("token123", result.token());
        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken(any());
    }

    @Test
    @DisplayName("""
            login | validate that method throw AuthenticationException
            when credentials invalid
            """)
    void login_invalidCredentials() {
        // given
        UserLoginRequestDto request = new UserLoginRequestDto(
                "john@example.com", "wrong"
        );

        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when + then
        assertThrows(
                AuthenticationException.class,
                () -> authenticationService.login(request)
        );

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtUtil);
    }
}

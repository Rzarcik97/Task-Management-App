package taskmanagement.security;

import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import taskmanagement.model.User;
import taskmanagement.model.UserVerificationToken;
import taskmanagement.repository.VerificationTokenRepository;

@Log4j2
@Component
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public String createToken(User user, UserVerificationToken.TokenType type,String newValue) {
        log.info("Starting creating verification token for user: id = {}", user.getId());
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        String rawCode = generateVerificationCode();
        String hashedCode = passwordEncoder.encode(rawCode);
        UserVerificationToken token = new UserVerificationToken();
        token.setUser(user);
        token.setVerificationCode(hashedCode);
        token.setType(type);
        token.setExpirationTime(LocalDateTime.now().plusMinutes(20));
        token.setNewValue(newValue);
        tokenRepository.save(token);
        log.info("Token created successfully");
        return rawCode;
    }

    public boolean validateCode(User user, String rawCode, UserVerificationToken.TokenType type) {
        log.info("checking verification code for user: id = {}", user.getId());
        return tokenRepository.findByUserAndType(user, type)
                .filter(t -> t.getExpirationTime().isAfter(LocalDateTime.now()))
                .map(t -> passwordEncoder.matches(rawCode, t.getVerificationCode()))
                .orElse(false);
    }

    private String generateVerificationCode() {
        int code = new Random().nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}

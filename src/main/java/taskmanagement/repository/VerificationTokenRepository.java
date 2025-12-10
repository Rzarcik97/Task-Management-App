package taskmanagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import taskmanagement.model.User;
import taskmanagement.model.UserVerificationToken;

public interface VerificationTokenRepository extends JpaRepository<UserVerificationToken,Long> {
    Optional<UserVerificationToken> findByUserAndType(User user,
                                                      UserVerificationToken.TokenType type);

    Optional<UserVerificationToken> findByUser(User user);
}


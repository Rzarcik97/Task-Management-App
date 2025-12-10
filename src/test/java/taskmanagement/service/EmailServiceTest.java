package taskmanagement.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.mail.internet.MimeMessage;
import java.nio.file.Files;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import taskmanagement.model.Project;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.service.impl.EmailServiceImpl;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private Files files;

    @Spy
    @InjectMocks
    private EmailServiceImpl emailServiceImpl;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(emailServiceImpl, "fromEmail", "noreply@test.com");
    }

    @Test
    @DisplayName("sendTaskReminder | should load template, fill it and send email")
    void sendTaskReminder_success() {
        // given
        User user = new User();
        user.setUsername("John");
        user.setEmail("test@example.com");

        Task task = new Task();
        task.setName("Task X");
        task.setDueDate(LocalDate.of(2025, 1, 10));

        doReturn(mock(MimeMessage.class))
                .when(mailSender).createMimeMessage();
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        emailServiceImpl.sendTaskReminder(user, task);

        // then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @DisplayName("""
            sendPasswordChangeVerification | should load template, fill values and send email
            """)
    @Test
    void sendPasswordChangeVerification_success() throws Exception {
        // given
        User user = new User();
        user.setUsername("John");
        user.setEmail("john@example.com");

        String code = "123456";

        doReturn(mock(MimeMessage.class))
                .when(mailSender).createMimeMessage();
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        emailServiceImpl.sendPasswordChangeVerification(user, code);

        // then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @DisplayName("""
            sendEmailChangeVerification | should load template, fill values and send email
            """)
    @Test
    void sendEmailChangeVerification_success() throws Exception {
        // given
        User user = new User();
        user.setUsername("John");
        user.setEmail("old@example.com");

        String newEmail = "new@example.com";
        String code = "ABC123";

        doReturn(mock(MimeMessage.class))
                .when(mailSender).createMimeMessage();
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        emailServiceImpl.sendEmailChangeVerification(user, newEmail, code);

        // then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @DisplayName("""
            sendNewTaskAssigned | should load template, fill values and send email
            """)
    @Test
    void sendNewTaskAssigned_success() throws Exception {
        // given
        User user = new User();
        user.setUsername("John");
        user.setEmail("john@example.com");

        Project project = new Project();
        project.setName("Project X");

        Task task = new Task();
        task.setName("Task Test");
        task.setDueDate(LocalDate.of(2025, 1, 20));
        task.setProject(project);

        doReturn(mock(MimeMessage.class))
                .when(mailSender).createMimeMessage();

        // when
        emailServiceImpl.sendNewTaskAssigned(user, task);

        // then
        verify(mailSender).send(any(MimeMessage.class));
    }
}

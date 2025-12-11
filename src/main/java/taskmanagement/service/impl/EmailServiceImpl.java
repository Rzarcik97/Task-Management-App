package taskmanagement.service.impl;

import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import taskmanagement.exceptions.EmailSendingException;
import taskmanagement.exceptions.TemplatesLoadException;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.service.EmailService;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from.address}")
    private String fromEmail;

    @Override
    public void sendPasswordChangeVerification(User user, String code) {
        String template = loadTemplate("password_change.html");
        String content = fillTemplate(template, Map.of(
                "username", user.getUsername(),
                "verificationCode", code
        ));
        sendHtmlEmail(user.getEmail(), "Password Change Verification Code", content);
    }

    @Override
    public void sendEmailChangeVerification(User user,String newEmail, String code) {
        String template = loadTemplate("email_change.html");
        String content = fillTemplate(template, Map.of(
                "username", user.getUsername(),
                "newEmail", newEmail,
                "verificationCode", code
        ));
        sendHtmlEmail(user.getEmail(), "Email Change Verification Code", content);

    }

    @Override
    public void sendTaskReminder(User user, Task task) {
        String template = loadTemplate("task_reminder.html");
        String content = fillTemplate(template, Map.of(
                "username", user.getUsername(),
                "taskName", task.getName(),
                "dueDate", task.getDueDate().toString()
        ));
        sendHtmlEmail(user.getEmail(), "Task Reminder: " + task.getName(), content);
    }

    @Override
    public void sendNewTaskAssigned(User user, Task task) {
        String template = loadTemplate("task_assigned.html");
        String content = fillTemplate(template, Map.of(
                "username", user.getUsername(),
                "taskName", task.getName(),
                "projectName", task.getProject().getName(),
                "dueDate", task.getDueDate().toString()
        ));
        sendHtmlEmail(user.getEmail(), "New Task Assigned: " + task.getName(), content);
    }

    private void sendHtmlEmail(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (Exception e) {
            throw new EmailSendingException("Failed to send email to: " + toEmail);
        }
    }

    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + path);
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new TemplatesLoadException("Template" + path + " not found");
        }
    }

    private String fillTemplate(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return template;
    }
}

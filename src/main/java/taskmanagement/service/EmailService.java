package taskmanagement.service;

import taskmanagement.model.Task;
import taskmanagement.model.User;

public interface EmailService {
    void sendPasswordChangeVerification(User user, String token);

    void sendEmailChangeVerification(User user, String newEmail, String token);

    void sendTaskReminder(User user, Task task);

    void sendNewTaskAssigned(User user, Task task);
}

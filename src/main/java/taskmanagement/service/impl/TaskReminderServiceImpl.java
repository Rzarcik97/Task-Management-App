package taskmanagement.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.TaskRepository;
import taskmanagement.service.EmailService;
import taskmanagement.service.TaskReminderService;

@Service
@Log4j2
@RequiredArgsConstructor
public class TaskReminderServiceImpl implements TaskReminderService {

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    @Override
    @Scheduled(cron = "0 0 8 * * *")
    public void sendTaskReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Checking for tasks due on {}", tomorrow);
        List<Task> tasksDueTomorrow = taskRepository.findByDueDate(tomorrow);
        if (tasksDueTomorrow.isEmpty()) {
            log.info("No tasks due for tomorrow.");
            return;
        }
        tasksDueTomorrow.forEach(task -> {
            User assignee = task.getAssignee();
            if (assignee != null) {
                log.info("Sending reminder for task '{}'", task.getName());
                emailService.sendTaskReminder(assignee, task);
            }
        });
    }
}

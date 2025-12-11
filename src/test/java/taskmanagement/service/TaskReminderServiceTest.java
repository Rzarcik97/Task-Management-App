package taskmanagement.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.TaskRepository;
import taskmanagement.service.impl.TaskReminderServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TaskReminderServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TaskReminderServiceImpl taskReminderService;

    @Test
    @DisplayName("""
            sendTaskReminders | verify that method do nothing
             when no tasks are due tomorrow
            """)
    void sendTaskReminders_noTasks() {
        // given
        LocalDate tomorrow = LocalDate.of(2024, 1, 2);

        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class)) {
            mockedDate.when(LocalDate::now).thenReturn(tomorrow.minusDays(1));

            when(taskRepository.findByDueDate(tomorrow)).thenReturn(List.of());

            // when
            taskReminderService.sendTaskReminders();

            // then
            verify(taskRepository).findByDueDate(tomorrow);
            verifyNoInteractions(emailService);
        }
    }

    @Test
    @DisplayName("""
            sendTaskReminders | verify that method send reminder for each task with assignee
            """)
    void sendTaskReminders_success() {
        // given
        LocalDate tomorrow = LocalDate.of(2024, 1, 2);

        User user = new User();
        user.setEmail("test@example.com");

        User assignee = new User();
        assignee.setEmail("assignee@example.com");

        Task task = new Task();
        task.setName("Task A");
        task.setAssignee(user);

        Task task2 = new Task();
        task2.setName("Task B");
        task2.setAssignee(assignee);

        List<Task> tasks = List.of(task, task2);

        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class)) {
            mockedDate.when(LocalDate::now).thenReturn(tomorrow.minusDays(1));

            when(taskRepository.findByDueDate(tomorrow)).thenReturn(tasks);

            // when
            taskReminderService.sendTaskReminders();

            // then
            verify(taskRepository).findByDueDate(tomorrow);
            verify(emailService, times(2))
                    .sendTaskReminder(any(User.class), any(Task.class));
        }
    }

    @Test
    @DisplayName("""
            sendTaskReminders | verify that method skip tasks without assignee
            """)
    void sendTaskReminders_noAssignee() {
        // given
        LocalDate tomorrow = LocalDate.of(2024, 1, 2);

        Task t1 = new Task();
        t1.setName("Task A");
        t1.setAssignee(null);

        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class)) {
            mockedDate.when(LocalDate::now).thenReturn(tomorrow.minusDays(1));

            when(taskRepository.findByDueDate(tomorrow)).thenReturn(List.of(t1));

            // when
            taskReminderService.sendTaskReminders();

            // then
            verify(taskRepository).findByDueDate(tomorrow);
            verifyNoInteractions(emailService);
        }
    }
}

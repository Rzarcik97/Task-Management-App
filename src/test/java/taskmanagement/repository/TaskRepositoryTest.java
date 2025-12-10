package taskmanagement.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import taskmanagement.model.Task;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("""
            findByIdWithRelations | should return task with project and assignee fetched
            """)
    void findByIdWithRelations_success() {
        // given
        Long existingId = 1L;

        // when
        Optional<Task> result = taskRepository.findByIdWithRelations(existingId);

        // then
        assertTrue("Task should exist", result.isPresent());
        Task task = result.get();

        assertNotNull(task.getProject(), "Project must be fetched");
        assertNotNull(task.getProject().getId());
        assertNotNull(task.getAssignee(), "Assignee must be fetched");
        assertNotNull(task.getAssignee().getEmail());
    }

    @Test
    @DisplayName("""
            findByIdWithRelations | should return empty optional when task not found
            """)
    void findByIdWithRelations_notFound() {
        // when
        Optional<Task> result = taskRepository.findByIdWithRelations(6L);

        // then
        assertTrue("Should return empty Optional", result.isEmpty());
    }
}

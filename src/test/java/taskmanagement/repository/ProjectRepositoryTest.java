package taskmanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import taskmanagement.model.Project;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProjectRepositoryTest {
    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("""
            findAllByMemberEmail | should return projects assigned to user from liquibase
            """)
    void findAllByMemberEmail_fromLiquibase() {

        // when
        List<Project> projects =
                projectRepository.findAllByMemberEmail("john.doe@example.com");

        // then
        assertEquals(2, projects.size());
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("Task Management System")));
    }

    @Test
    @DisplayName("""
            findAllByMemberEmail | should return empty list if user exists but has no projects
            """)
    void findAllByMemberEmail_noProjects() {

        // when
        List<Project> projects =
                projectRepository.findAllByMemberEmail("anna@example.com");

        // then
        assertEquals(0, projects.size());
        assertTrue(projects.isEmpty());
    }

    @Test
    @DisplayName("""
            findAllByMemberEmail | should return empty list for unknown email
            """)
    void findAllByMemberEmail_unknownEmail() {

        // when
        List<Project> projects =
                projectRepository.findAllByMemberEmail("nobody@example.com");

        // then
        assertTrue(projects.isEmpty());
    }
}

package taskmanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<Project> projects =
                projectRepository.findAllByMemberEmail("john.doe@example.com", pageable);

        // then
        assertEquals(2, projects.getTotalElements());
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("Task Management System")));
    }

    @Test
    @DisplayName("""
            findAllByMemberEmail | should return empty list if user exists but has no projects
            """)
    void findAllByMemberEmail_noProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<Project> projects =
                projectRepository.findAllByMemberEmail("anna@example.com", pageable);

        // then
        assertEquals(0, projects.getTotalElements());
        assertTrue(projects.isEmpty());
    }

    @Test
    @DisplayName("""
            findAllByMemberEmail | should return empty list for unknown email
            """)
    void findAllByMemberEmail_unknownEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<Project> projects =
                projectRepository.findAllByMemberEmail("nobody@example.com", pageable);

        // then
        assertTrue(projects.isEmpty());
    }
}

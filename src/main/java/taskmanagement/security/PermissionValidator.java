package taskmanagement.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.model.Project;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.User;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.UserRepository;

@Log4j2
@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public void validateAccess(String email, Long projectId, ProjectMember.Role accessRole) {
        log.info("starting validation of access for user: email = {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + email + " not found"));

        if (user.getRole() == User.Role.ADMIN) {
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id: " + projectId + " not found"));

        ProjectMember member = project.getMembers().stream()
                .filter(m -> m.getUser().equals(user))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("User is not part of this project"));

        boolean hasAccess = accessRole.getRank() <= member.getRole().getRank();

        if (!hasAccess) {
            throw new AccessDeniedException("You don't have permission to perform this action");
        }
        log.info("access granted");
    }
}

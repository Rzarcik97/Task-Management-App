package taskmanagement.service.impl;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import taskmanagement.dto.project.ProjectMemberRequest;
import taskmanagement.dto.project.ProjectPatchRequestDto;
import taskmanagement.dto.project.ProjectRequestDto;
import taskmanagement.dto.project.ProjectResponseDto;
import taskmanagement.dto.project.ProjectSummaryDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.ProjectMapper;
import taskmanagement.mapper.UserMapper;
import taskmanagement.model.Project;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.User;
import taskmanagement.repository.ProjectMemberRepository;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.ProjectService;

@Log4j2
@Transactional
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final PermissionValidator permissionValidator;

    @Override
    public ProjectResponseDto createProject(ProjectRequestDto request,
                                            String projectMemberManagerEmail,
                                            String email) {
        log.info("Starting creating project: name = {}", request.name());
        Project project = projectMapper.toModel(request);
        project.setStatus(Project.Status.INITIATED);
        project.setStartDate(LocalDate.now());
        Project savedProject = projectRepository.save(project);
        ProjectMemberRequest memberRequest = new ProjectMemberRequest(
                projectMemberManagerEmail != null ? projectMemberManagerEmail : email,
                ProjectMember.Role.MANAGER
        );
        addMemberToProject(savedProject.getId(), memberRequest, email);
        log.info("project created successfully: id = {}", savedProject.getId());
        return getProjectById(savedProject.getId());
    }

    @Override
    public UserResponseDto addMemberToProject(Long projectId,
                                              ProjectMemberRequest member,
                                              String email) {
        log.info("Starting Adding members to project: id = {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id: " + projectId + " not found"));
        permissionValidator.validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        ProjectMember newMember = new ProjectMember();
        newMember.setProject(project);
        newMember.setRole(member.role());
        User findededUser = userRepository.findByEmail(member.memberEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with username:" + member.memberEmail() + " not found"));
        newMember.setUser(findededUser);
        projectMemberRepository.save(newMember);
        project.getMembers().add(newMember);
        log.info("members added successfully");
        return userMapper.toDto(findededUser);
    }

    @Override
    public void deleteMemberFromProject(Long projectId, String memberEmail, String email) {
        log.info("Starting deleting members from project: id = {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id: " + projectId + " not found"));
        permissionValidator.validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        ProjectMember memberToRemove = project.getMembers().stream()
                .filter(m -> m.getUser().getEmail().equals(memberEmail))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email: " + memberEmail + " is not part of this project"));
        project.getMembers().remove(memberToRemove);
        log.info("members deleted successfully");
    }

    @Override
    public List<ProjectSummaryDto> getUserProjects(String email) {
        return projectRepository.findAllByMemberEmail(email).stream()
                .map(project -> new ProjectSummaryDto(project.getId(),
                        project.getName()))
                .toList();
    }

    @Override
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id: " + id + " not found"));
        return projectMapper.toDto(project);
    }

    @Override
    public ProjectResponseDto updateProject(Long projectId,
                                            ProjectPatchRequestDto request,
                                            String email) {
        log.info("Starting editing project: id = {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id: " + projectId + " not found"));
        permissionValidator.validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        projectMapper.updateFromPatch(request, project);
        Project updatedProject = projectRepository.save(project);
        log.info("project edited successfully");
        return projectMapper.toDto(updatedProject);
    }

    @Override
    public void deleteProject(Long projectId, String email) {
        log.info("Starting deleting project: id = {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Project with id: " + projectId + " not found"));
        permissionValidator.validateAccess(email, project.getId(), ProjectMember.Role.MANAGER);
        projectRepository.deleteById(projectId);
        log.info("project deleted successfully");
    }
}

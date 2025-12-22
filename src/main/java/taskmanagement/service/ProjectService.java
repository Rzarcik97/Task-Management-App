package taskmanagement.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import taskmanagement.dto.project.ProjectMemberRequest;
import taskmanagement.dto.project.ProjectPatchRequestDto;
import taskmanagement.dto.project.ProjectRequestDto;
import taskmanagement.dto.project.ProjectResponseDto;
import taskmanagement.dto.project.ProjectSummaryDto;
import taskmanagement.dto.user.UserResponseDto;

public interface ProjectService {

    ProjectResponseDto createProject(ProjectRequestDto request,
                                     String projectMemberUsername,
                                     String email);

    UserResponseDto addMemberToProject(Long id, ProjectMemberRequest member, String email);

    void deleteMemberFromProject(Long id, String memberEmail, String email);

    List<ProjectSummaryDto> getUserProjects(String email, Pageable pageable);

    ProjectResponseDto getProjectById(Long id);

    ProjectResponseDto updateProject(Long id, ProjectPatchRequestDto request, String email);

    void deleteProject(Long id, String email);
}

package taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.project.ProjectMemberRequest;
import taskmanagement.dto.project.ProjectPatchRequestDto;
import taskmanagement.dto.project.ProjectRequestDto;
import taskmanagement.dto.project.ProjectResponseDto;
import taskmanagement.dto.project.ProjectSummaryDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.service.ProjectService;

@Log4j2
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Project",
            description = "Create a new project and add project manager "
                    + "to this project(only for users with ADMIN role)")
    public ProjectResponseDto createProject(
            @RequestBody @Valid ProjectRequestDto request,
            @RequestParam(required = false) String projectManagerEmail,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Creating project {}, by User {}", request.name(), email);
        return projectService.createProject(request,projectManagerEmail,email);
    }

    @GetMapping
    @PageableAsQueryParam
    @Operation(summary = "Get User Projects",
            description = "Retrieve all projects where the current user is a member")
    public List<ProjectSummaryDto> getUserProjects(Authentication authentication,
                                                   @ParameterObject Pageable pageable) {
        String email = authentication.getName();
        return projectService.getUserProjects(email, pageable);
    }

    @GetMapping("/{projectId}")
    @Operation(
            summary = "Get Project Details",
            description = "Retrieve details of a specific project by ID"
    )
    public ProjectResponseDto getProjectById(@PathVariable Long projectId) {
        return projectService.getProjectById(projectId);
    }

    @PostMapping("/{projectId}/member")
    @Operation(
            summary = "Add new Member to Project by ID",
            description = "Add new Member to existing Project by ID "
                    + "(ADMIN or project MANAGER only)"
    )
    public UserResponseDto addMemberToProject(@PathVariable Long projectId,
                                              @RequestBody @Valid ProjectMemberRequest member,
                                              Authentication authentication) {
        String email = authentication.getName();
        log.info("Adding member {} to project {}, by User {}",
                member.memberEmail(), projectId, email);
        return projectService.addMemberToProject(projectId,member,email);
    }

    @DeleteMapping("/{projectId}/member")
    @Operation(
            summary = "Delete Member from Project by ID",
            description = "Delete Member from existing Project by ID "
                    + "(ADMIN or project MANAGER only)"
    )
    public void deleteMemberFromProject(@PathVariable Long projectId,
                                              @RequestParam String memberEmail,
                                              Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting member {} from project {}, by User {}",
                memberEmail, projectId, email);
        projectService.deleteMemberFromProject(projectId,memberEmail,email);
    }

    @PatchMapping("/{projectId}")
    @Operation(summary = "Update Project by ID",
            description = "Update details of an existing project (ADMIN or project MANAGER only)")
    public ProjectResponseDto updateProject(@PathVariable Long projectId,
                                            @RequestBody @Valid ProjectPatchRequestDto request,
                                            Authentication authentication) {
        String email = authentication.getName();
        log.info("Editing project {}, by User {}", projectId, email);
        return projectService.updateProject(projectId, request,email);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete Project",
            description = "Delete a project by ID (ADMIN or project MANAGER only)")
    public void deleteProject(@PathVariable Long projectId,
                              Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting project {}, by User {}", projectId, email);
        projectService.deleteProject(projectId,email);
    }
}

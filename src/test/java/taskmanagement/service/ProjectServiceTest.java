package taskmanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import taskmanagement.dto.project.ProjectMemberRequest;
import taskmanagement.dto.project.ProjectPatchRequestDto;
import taskmanagement.dto.project.ProjectRequestDto;
import taskmanagement.dto.project.ProjectResponseDto;
import taskmanagement.dto.project.ProjectSummaryDto;
import taskmanagement.dto.user.UserResponseDto;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.ProjectMapper;
import taskmanagement.mapper.ProjectMemberMapper;
import taskmanagement.mapper.UserMapper;
import taskmanagement.mapper.impl.ProjectMapperImpl;
import taskmanagement.mapper.impl.ProjectMemberMapperImpl;
import taskmanagement.mapper.impl.UserMapperImpl;
import taskmanagement.model.Project;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.User;
import taskmanagement.repository.ProjectMemberRepository;
import taskmanagement.repository.ProjectRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.impl.ProjectServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionValidator permissionValidator;

    @Spy
    @InjectMocks
    private ProjectServiceImpl projectServiceImpl;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Spy
    private ProjectMemberMapper projectMemberMapper =
            new ProjectMemberMapperImpl();

    @Spy
    private ProjectMapper projectMapper =
            new ProjectMapperImpl(projectMemberMapper);

    @Test
    @DisplayName("""
            createProject | validate that method create project,
            add manager and return project dto
            """)
    void createProject_validRequest_success() {
        // given
        ProjectRequestDto request = new ProjectRequestDto(
                "Test Project",
                null,
                null
        );

        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        User creator = new User();
        creator.setEmail("creator@email.com");
        creator.setRole(User.Role.ADMIN);
        User manager = new User();
        manager.setEmail("manager@email.com");

        ProjectMemberRequest memberRequest = new ProjectMemberRequest(
                manager.getEmail(),
                ProjectMember.Role.MANAGER
        );

        LocalDate now = LocalDate.of(2024, 1, 10);

        ProjectResponseDto expected = new ProjectResponseDto(
                1L,
                "Test Project",
                null,
                now,
                null,
                Project.Status.INITIATED,
                List.of()
        );

        try (MockedStatic<LocalDate> mockedDate = Mockito.mockStatic(LocalDate.class)) {
            mockedDate.when(LocalDate::now).thenReturn(now);

            when(projectMapper.toModel(request)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);

            doReturn(expected)
                    .when(projectServiceImpl)
                    .getProjectById(project.getId());

            doReturn(null)
                    .when(projectServiceImpl)
                    .addMemberToProject(project.getId(), memberRequest, creator.getEmail());

            // when
            ProjectResponseDto actual = projectServiceImpl.createProject(
                    request,
                    manager.getEmail(),
                    creator.getEmail()
            );

            // then
            assertThat(actual).isEqualTo(expected);

            verify(projectMapper,times(1)).toModel(request);
            verify(projectRepository,times(1)).save(project);
            verify(projectServiceImpl,times(1)).addMemberToProject(
                    project.getId(),
                    memberRequest,
                    creator.getEmail()
            );
            verify(projectServiceImpl).getProjectById(project.getId());
        }
    }

    @Test
    @DisplayName("""
            createProject | validate that when manager email is null,
            project creator is assigned as manager
            """)
    void createProject_managerNull_assignsCreatorAsManager() {

        // given
        ProjectRequestDto request = new ProjectRequestDto(
                "Test Project",
                null,
                null
        );

        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        User creator = new User();
        creator.setEmail("creator@email.com");
        creator.setRole(User.Role.ADMIN);

        LocalDate now = LocalDate.of(2024, 1, 10);

        ProjectResponseDto expected = new ProjectResponseDto(
                1L,
                "Test Project",
                null,
                now,
                null,
                Project.Status.INITIATED,
                List.of()
        );

        try (MockedStatic<LocalDate> mockedDate = Mockito.mockStatic(LocalDate.class)) {
            mockedDate.when(LocalDate::now).thenReturn(now);

            when(projectMapper.toModel(request)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);

            doReturn(expected)
                    .when(projectServiceImpl)
                    .getProjectById(project.getId());

            doReturn(null)
                    .when(projectServiceImpl)
                    .addMemberToProject(eq(project.getId()), any(), eq(creator.getEmail()));

            // when
            ProjectResponseDto actual = projectServiceImpl.createProject(
                    request,
                    null,
                    creator.getEmail()
            );

            // then
            assertThat(actual).isEqualTo(expected);

            verify(projectMapper, times(1)).toModel(request);
            verify(projectRepository, times(1)).save(project);
            verify(projectServiceImpl, times(1)).addMemberToProject(
                    eq(project.getId()),
                    any(),
                    eq(creator.getEmail())
            );
            verify(projectServiceImpl).getProjectById(project.getId());
        }
    }

    @Test
    @DisplayName("""
            addMemberToProject | validate that method adds member,
            assigns role and returns UserResponseDto
            """)
    void addMemberToProject_validInput_success() {
        // Given
        Long projectId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setMembers(new HashSet<>() {
        });

        String newMemberEmail = "john@example.com";

        User foundUser = new User();
        foundUser.setId(1L);
        foundUser.setEmail(newMemberEmail);
        foundUser.setRole(User.Role.USER);

        ProjectMember savedMember = new ProjectMember();
        savedMember.setId(1L);
        savedMember.setRole(ProjectMember.Role.MEMBER);
        savedMember.setUser(foundUser);
        savedMember.setProject(project);

        UserResponseDto expectedDto = new UserResponseDto(
                foundUser.getId(),
                null,
                foundUser.getEmail(),
                null,
                null,
                foundUser.getRole().toString()
        );

        String requesterEmail = "manager@example.com";

        ProjectMemberRequest request = new ProjectMemberRequest(
                    newMemberEmail,
                    ProjectMember.Role.MEMBER
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator).validateAccess(any(), any(), any());
        when(userRepository.findByEmail(newMemberEmail)).thenReturn(Optional.of(foundUser));
        when(projectMemberRepository.save(any(ProjectMember.class)))
                .thenReturn(savedMember);

        // When
        UserResponseDto actual = projectServiceImpl.addMemberToProject(
                projectId, request, requesterEmail);

        // Then
        assertThat(actual).isEqualTo(expectedDto);
        assertThat(project.getMembers()).hasSize(1);

        verify(projectRepository,times(1)).findById(projectId);
        verify(permissionValidator,times(1)).validateAccess(
                requesterEmail, projectId, ProjectMember.Role.MANAGER);
        verify(userRepository,times(1)).findByEmail(newMemberEmail);
        verify(projectMemberRepository,times(1)).save(any(ProjectMember.class));
        verify(userMapper,times(1)).toDto(foundUser);
    }

    @Test
    @DisplayName("""
            addMemberToProject | validate that method throws when project not found
            """)
    void addMemberToProject_projectNotFound_throws() {
        // Given
        Long projectId = 1L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() ->
                projectServiceImpl.addMemberToProject(projectId,
                        new ProjectMemberRequest("john@example.com", ProjectMember.Role.MANAGER),
                           "manager@example.com")
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository).findById(projectId);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    @DisplayName("""
            addMemberToProject | validate that method throws when user not found
            """)
    void addMemberToProject_userNotFound_throws() {
        // Given
        Long projectId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setMembers(new HashSet<>());

        String newMemberEmail = "missing@example.com";
        String requesterEmail = "manager@example.com";

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator).validateAccess(any(), any(), any());
        when(userRepository.findByEmail(newMemberEmail)).thenReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() ->
                projectServiceImpl.addMemberToProject(
                        projectId,
                        new ProjectMemberRequest(newMemberEmail, ProjectMember.Role.VIEWER),
                        requesterEmail
                )
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository).findById(projectId);
        verify(permissionValidator).validateAccess(
                requesterEmail, projectId, ProjectMember.Role.MANAGER);
        verify(userRepository).findByEmail(newMemberEmail);
        verifyNoMoreInteractions(projectMemberRepository);
    }

    @Test
    @DisplayName("""
            deleteMemberFromProject | validate that member is removed
            from project when project + member exist and access granted
            """)
    void deleteMemberFromProject_validInputs_success() {
        // Given
        Long projectId = 1L;
        String requesterEmail = "manager@example.com";
        String memberEmail = "john@example.com";

        User manager = new User();
        manager.setEmail(requesterEmail);

        User member = new User();
        member.setEmail(memberEmail);

        Project project = new Project();
        project.setId(projectId);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setUser(member);
        projectMember.setProject(project);

        project.setMembers(new HashSet<>(Set.of(projectMember)));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator).validateAccess(any(), any(), any());

        // When
        projectServiceImpl.deleteMemberFromProject(projectId, memberEmail, requesterEmail);

        // Then
        assertThat(project.getMembers()).isEmpty();
        verify(projectRepository,times(1)).findById(projectId);
        verify(permissionValidator,times(1)).validateAccess(
                requesterEmail, projectId, ProjectMember.Role.MANAGER);
        verifyNoMoreInteractions(projectRepository, permissionValidator);
    }

    @DisplayName("""
            deleteMemberFromProject | throw when project does not exist
            """)
    @Test
    void deleteMemberFromProject_projectNotFound_throwsEntityNotFound() {
        // Given
        Long projectId = 1L;
        String requesterEmail = "manager@example.com";
        String memberEmail = "john@example.com";
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() ->
                projectServiceImpl.deleteMemberFromProject(projectId, memberEmail, requesterEmail)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository,times(1)).findById(projectId);
        verifyNoMoreInteractions(projectRepository);
    }

    @DisplayName("""
            deleteMemberFromProject | throw when member is not part of project
            """)
    @Test
    void deleteMemberFromProject_memberNotFound_throwsEntityNotFound() {
        // Given
        Long projectId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setMembers(new HashSet<>());

        String requesterEmail = "manager@mail.com";
        String memberEmail = "missing@mail.com";

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(permissionValidator).validateAccess(any(), any(), any());

        // When / Then
        assertThatThrownBy(() ->
                projectServiceImpl.deleteMemberFromProject(projectId, memberEmail, requesterEmail)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository,times(1)).findById(projectId);
        verify(permissionValidator,times(1)).validateAccess(
                requesterEmail, projectId, ProjectMember.Role.MANAGER);
    }

    @Test
    @DisplayName("""
            getUserProjects | verify that method returns list of projects
            belonging to user
            """)
    void getUserProjects_returnsListOfProjects() {
        // Given

        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("Project A");

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("Project B");

        String email = "john@example.com";

        when(projectRepository.findAllByMemberEmail(email))
                .thenReturn(List.of(p1, p2));

        // When
        List<ProjectSummaryDto> actual = projectServiceImpl.getUserProjects(email);

        // Then
        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(ProjectSummaryDto::id)
                .containsExactly(1L, 2L);
        assertThat(actual).extracting(ProjectSummaryDto::name)
                .containsExactly("Project A", "Project B");

        verify(projectRepository,times(1)).findAllByMemberEmail(email);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    @DisplayName("""
            getUserProjects | verify method returns empty list
            when user has no projects
            """)
    void getUserProjects_returnsEmptyListWhenNoProjects() {
        // Given
        String email = "john@example.com";

        when(projectRepository.findAllByMemberEmail(email))
                .thenReturn(List.of());

        // When
        List<ProjectSummaryDto> actual = projectServiceImpl.getUserProjects(email);

        // Then
        assertThat(actual).isEmpty();

        verify(projectRepository,times(1)).findAllByMemberEmail(email);
        verifyNoMoreInteractions(projectRepository);
    }

    @DisplayName("""
            getProjectById | validate that method returns project dto
            when project exists
            """)
    @Test
    void getProjectById_validId_returnsProjectDto() {
        // Given
        Long projectId = 1L;

        Project project = new Project();
        project.setId(projectId);
        project.setName("Test project");
        project.setStatus(Project.Status.INITIATED);

        ProjectResponseDto responseDto = new ProjectResponseDto(
                projectId,
                "Test project",
                null,
                null,
                null,
                Project.Status.INITIATED,
                List.of()
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When
        ProjectResponseDto actual = projectServiceImpl.getProjectById(projectId);

        // Then
        assertThat(actual).isEqualTo(responseDto);

        verify(projectRepository,times(1)).findById(projectId);
        verify(projectMapper,times(1)).toDto(project);
        verifyNoMoreInteractions(projectRepository);
    }

    @DisplayName("""
            getProjectById | validate that method throws exception
            when project does not exist
            """)
    @Test
    void getProjectById_notFound_throwsException() {
        // Given
        Long projectId = 1L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> projectServiceImpl.getProjectById(projectId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository).findById(projectId);
        verifyNoInteractions(projectMapper);
    }

    @Test
    @DisplayName("""
            updateProject | validate that project is found,
            permission validated, patched, saved and dto returned
            """)
    public void updateProject_validRequest_success() {
        // given
        Long projectId = 1L;

        Project existing = new Project();
        existing.setId(projectId);
        existing.setName("Old name");
        existing.setDescription("Old desc");
        existing.setStatus(Project.Status.INITIATED);

        LocalDate newStartDay = LocalDate.of(2024, 1, 10);
        LocalDate newEndDay = LocalDate.of(2024, 1, 13);

        Project updated = new Project();
        updated.setId(projectId);
        updated.setName("Updated name");
        updated.setStartDate(newStartDay);
        updated.setEndDate(newEndDay);
        updated.setStatus(Project.Status.IN_PROGRESS);

        ProjectResponseDto expectedDto = new ProjectResponseDto(
                projectId,
                "Updated name",
                "Old desc",
                newStartDay,
                newEndDay,
                Project.Status.IN_PROGRESS,
                List.of()
        );

        String email = "manager@example.com";

        ProjectPatchRequestDto patchDto = new ProjectPatchRequestDto(
                "Updated name",
                "Updated description",
                newStartDay,
                newEndDay,
                Project.Status.IN_PROGRESS
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(updated);
        when(projectMapper.toDto(updated)).thenReturn(expectedDto);
        doNothing().when(permissionValidator).validateAccess(
                email,
                projectId,
                ProjectMember.Role.MANAGER);

        // when
        ProjectResponseDto actual = projectServiceImpl.updateProject(
                projectId,
                patchDto,
                email
        );

        // then
        assertThat(actual).isEqualTo(expectedDto);

        verify(projectRepository).findById(projectId);
        verify(permissionValidator)
                .validateAccess(email, projectId, ProjectMember.Role.MANAGER);

        verify(projectMapper,times(1)).updateFromPatch(patchDto, existing);
        verify(projectRepository,times(1)).save(existing);
    }

    @Test
    @DisplayName("""
            updateProject | validate that method throw EntityNotFoundException
            """)
    void updateProject_projectNotFound_throwsException() {
        // given
        Long projectId = 10L;
        String email = "john@example.com";

        ProjectPatchRequestDto patchDto = new ProjectPatchRequestDto(
                "New name",
                "desc",
                null,
                null,
                Project.Status.IN_PROGRESS
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->
                projectServiceImpl.updateProject(projectId, patchDto, email)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository,times(1)).findById(projectId);
        verifyNoInteractions(permissionValidator,projectMapper);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    @DisplayName("""
            deleteProject | validate that project exists, permission granted and delete is invoked
            """)
    void deleteProject_validRequest_success() {
        // given
        Long projectId = 1L;
        String email = "manager@example.com";

        Project project = new Project();
        project.setId(projectId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // when
        projectServiceImpl.deleteProject(projectId, email);

        // then
        verify(projectRepository,times(1)).findById(projectId);
        verify(permissionValidator,times(1))
                .validateAccess(email, projectId, ProjectMember.Role.MANAGER);
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    @DisplayName("""
            deleteProject | project not found | should throw EntityNotFoundException
            """)
    void deleteProject_projectNotFound_throwsException() {
        // given
        Long projectId = 1L;
        String email = "john@example.com";

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() ->
                projectServiceImpl.deleteProject(projectId, email)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository,times(1)).findById(projectId);
        verifyNoInteractions(permissionValidator);
        verify(projectRepository, never()).deleteById(any());
    }

}

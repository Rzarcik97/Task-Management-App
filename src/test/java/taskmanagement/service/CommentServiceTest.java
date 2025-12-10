package taskmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import taskmanagement.dto.comment.CommentRequestDto;
import taskmanagement.dto.comment.CommentResponseDto;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.CommentMapper;
import taskmanagement.mapper.impl.CommentMapperImpl;
import taskmanagement.model.Comment;
import taskmanagement.model.Project;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.CommentRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.impl.CommentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PermissionValidator permissionValidator;

    @Spy
    private CommentMapper commentMapper = new CommentMapperImpl();

    @InjectMocks
    private CommentServiceImpl commentServiceImpl;

    @Test
    @DisplayName("""
            addComment |  verify that method save comment and return dto with valid entry
            """)
    void addComment_success() {
        // given
        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);

        Project project = new Project();
        project.setId(5L);

        Task task = new Task();
        task.setId(1L);
        task.setProject(project);
        task.setName("Test task");

        Comment saved = new Comment();
        saved.setId(1L);
        saved.setText("Test comment");
        saved.setUser(user);
        saved.setTask(task);
        saved.setTimestamp(LocalDateTime.of(2004,10,12,12,0));

        CommentRequestDto request = new CommentRequestDto(
                1L,
                "Test comment");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(taskRepository.findById(request.taskId())).thenReturn(Optional.of(task));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponseDto dto = new CommentResponseDto(saved.getId(),
                task.getName(),
                saved.getUser().getUsernameField(),
                saved.getText(),
                saved.getTimestamp());

        when(commentMapper.toDto(saved)).thenReturn(dto);

        // when
        CommentResponseDto result = commentServiceImpl.addComment(request, email);

        // then
        assertNotNull(result);
        assertEquals(saved.getId(), result.id());
        assertEquals(saved.getText(), result.text());

        verify(userRepository).findByEmail(email);
        verify(taskRepository).findById(request.taskId());
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        verify(commentRepository).save(any(Comment.class));
        verify(commentMapper).toDto(saved);
    }

    @Test
    @DisplayName("""
            addComment | verify that method throw EntityNotFoundException
             when user not found
            """)
    void addComment_userNotFound() {
        // given
        String email = "user@example.com";
        CommentRequestDto request = new CommentRequestDto(
                1L,
                "Hello");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.addComment(request, email));

        // verify
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(taskRepository);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("""
            addComment | verify that method throw EntityNotFoundException
             when task not found
            """)
    void addComment_taskNotFound() {
        // given
        String email = "user@example.com";
        CommentRequestDto request = new CommentRequestDto(
                1L,
                "Hello");

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(taskRepository.findById(request.taskId())).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.addComment(request, email));

        verify(userRepository).findByEmail(email);
        verify(taskRepository).findById(request.taskId());
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("""
            updateComment | verify that method updates authors own comment
             with valid entry
            """)
    void updateComment_success_author() {
        // given
        String email = "user@example.com";

        User user = new User();
        user.setUsername("username");
        user.setEmail(email);
        user.setRole(User.Role.USER);

        Long commentId = 1L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user);

        Comment saved = new Comment();
        saved.setId(commentId);
        saved.setUser(user);

        String text = "Test comment";

        CommentResponseDto dto = new CommentResponseDto(
                saved.getId(),
                user.getUsernameField(),
                email,
                text,
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentMapper.toDto(saved)).thenReturn(dto);

        // when
        CommentResponseDto result =
                commentServiceImpl.updateComment(commentId, text, email);

        // then
        assertNotNull(result);
        assertEquals(commentId, result.id());

        // verify
        verify(userRepository).findByEmail(email);
        verify(commentRepository).findById(1L);
        verify(commentMapper).updateFromPatch(text, comment);
        verify(commentRepository).save(comment);
        verify(commentMapper).toDto(saved);
    }

    @Test
    @DisplayName("""
            updateComment | verify that method throw EntityNotFoundException
             when comment not found
            """)
    void updateComment_commentNotFound() {
        // given
        String email = "user@example.com";
        Long commentId = 1L;

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.updateComment(commentId, "text", email));

        // verify
        verify(userRepository).findByEmail(email);
        verify(commentRepository).findById(commentId);
        verifyNoInteractions(commentMapper);
    }

    @Test
    @DisplayName("""
            updateComment | verify that method throws EntityNotFoundException
             when user not found
            """)
    void updateComment_userNotFound() {
        // given
        String email = "user@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.updateComment(1L, "text", email));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }

    @Test
    @DisplayName("""
            updateComment | verify that method throw AccessDeniedException
             when user is not comment author and not admin try update comment
            """)
    void updateComment_accessDenied() {
        // given
        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(User.Role.USER); // not admin

        User author = new User();
        author.setEmail("other@example.com");

        Long commentId = 1L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(author); // belongs to someone else

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> commentServiceImpl.updateComment(commentId, "new text", email));

        // verify
        verify(userRepository).findByEmail(email);
        verify(commentRepository).findById(commentId);
        verifyNoInteractions(commentMapper);
    }

    @Test
    @DisplayName("""
            getCommentsByTask | verify that method returns comment
             with valid entry
            """)
    void getCommentsByTask_success() {
        // given
        Long taskId = 1L;

        Project project = new Project();
        project.setId(1L);

        Task task = new Task();
        task.setId(taskId);
        task.setProject(project);
        task.setName("Test task");

        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);
        user.setUsername("username");

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setTask(task);
        comment1.setUser(user);
        comment1.setText("Test comment");
        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setTask(task);
        comment2.setUser(user);
        comment2.setText("Test comment2");

        List<Comment> comments = List.of(comment1, comment2);

        CommentResponseDto dto1 = new CommentResponseDto(
                comment1.getId(),
                comment1.getTask().getName(),
                comment1.getUser().getUsername(),
                comment1.getText(),
                LocalDateTime.of(2020, 1, 1, 3, 0));
        CommentResponseDto dto2 = new CommentResponseDto(
                comment2.getId(),
                comment2.getTask().getName(),
                comment2.getUser().getUsername(),
                comment2.getText(),
                LocalDateTime.of(2020, 1, 1, 6, 0));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        when(commentRepository.findByTask_Id(taskId)).thenReturn(comments);
        when(commentMapper.toDto(comment1)).thenReturn(dto1);
        when(commentMapper.toDto(comment2)).thenReturn(dto2);

        // when
        List<CommentResponseDto> result = commentServiceImpl.getCommentsByTask(taskId, email);

        // then
        assertEquals(2, result.size());
        assertEquals(comment1.getId(), result.get(0).id());
        assertEquals(comment2.getId(), result.get(1).id());

        // verify
        verify(taskRepository).findById(taskId);
        verify(permissionValidator)
                .validateAccess(email, project.getId(), ProjectMember.Role.VIEWER);
        verify(commentRepository).findByTask_Id(taskId);
        verify(commentMapper).toDto(comment1);
        verify(commentMapper).toDto(comment2);
    }

    @Test
    @DisplayName("""
            getCommentsByTask | validate that throw EntityNotFoundException
             when task not found
            """)
    void getCommentsByTask_taskNotFound() {
        // given
        Long taskId = 1L;
        String email = "user@example.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.getCommentsByTask(taskId, email));

        // verify
        verify(taskRepository).findById(taskId);
        verifyNoInteractions(permissionValidator);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("""
            deleteComment | validate that owner deletes comment successfully
            """)
    void deleteComment_ownerSuccess() {
        // given
        Long commentId = 1L;
        String email = "owner@example.com";

        User owner = new User();
        owner.setEmail(email);
        owner.setRole(User.Role.USER);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(owner);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(owner));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentServiceImpl.deleteComment(commentId, email);

        // then
        verify(userRepository).findByEmail(email);
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("""
            deleteComment | verify that method throw EntityNotFoundException
             when user not found
            """)
    void deleteComment_userNotFound() {
        // given
        Long commentId = 1L;
        String email = "user@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.deleteComment(commentId, email));

        // verify
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("""
            deleteComment | verify that method throw EntityNotFoundException
             when comment not found
            """)
    void deleteComment_commentNotFound() {
        // given
        Long commentId = 1L;
        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> commentServiceImpl.deleteComment(commentId, email));

        // verify
        verify(userRepository).findByEmail(email);
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("""
            deleteComment | verify that method throw AccessDeniedException
             when not owner and not admin deletes comment
            """)
    void deleteComment_accessDenied() {
        // given
        String email = "user@example.com";

        User caller = new User();
        caller.setEmail(email);
        caller.setRole(User.Role.USER);

        User owner = new User();
        owner.setEmail("owner@example.com");

        Long commentId = 1L;

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(owner);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(caller));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> commentServiceImpl.deleteComment(commentId, email));

        // verify
        verify(userRepository).findByEmail(email);
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }
}

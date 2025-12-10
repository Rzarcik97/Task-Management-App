package taskmanagement.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import taskmanagement.dto.comment.CommentRequestDto;
import taskmanagement.dto.comment.CommentResponseDto;
import taskmanagement.exceptions.AccessDeniedException;
import taskmanagement.exceptions.EntityNotFoundException;
import taskmanagement.mapper.CommentMapper;
import taskmanagement.model.Comment;
import taskmanagement.model.ProjectMember;
import taskmanagement.model.Task;
import taskmanagement.model.User;
import taskmanagement.repository.CommentRepository;
import taskmanagement.repository.TaskRepository;
import taskmanagement.repository.UserRepository;
import taskmanagement.security.PermissionValidator;
import taskmanagement.service.CommentService;

@Log4j2
@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final PermissionValidator permissionValidator;

    @Override
    public CommentResponseDto addComment(CommentRequestDto request, String email) {
        log.info("Starting adding comment to task with id = {}", request.taskId());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with email " + email + " not found"));
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + request.taskId() + " not found"));
        permissionValidator.validateAccess(email,
                task.getProject().getId(),
                ProjectMember.Role.VIEWER);
        Comment comment = new Comment();
        comment.setTask(task);
        comment.setUser(user);
        comment.setText(request.text());
        comment.setTimestamp(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment added successfully: id = {}", savedComment.getId());
        return commentMapper.toDto(savedComment);
    }

    @Override
    public CommentResponseDto updateComment(Long commentId, String text, String email) {
        log.info("Starting editing comment: id = {}", commentId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Comment commentToEdit = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id " + commentId + " not found"));
        if (!commentToEdit.getUser().getEmail().equals(email)
                && user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("You can only update your own comments");
        }
        commentToEdit.setTimestamp(LocalDateTime.now());
        commentMapper.updateFromPatch(text, commentToEdit);
        Comment editedComment = commentRepository.save(commentToEdit);
        log.info("Comment edited successfully");
        return commentMapper.toDto(editedComment);

    }

    @Override
    public List<CommentResponseDto> getCommentsByTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Task with id " + taskId + " not found"));
        permissionValidator.validateAccess(email,
                task.getProject().getId(),
                ProjectMember.Role.VIEWER);
        return commentRepository.findByTask_Id(taskId)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public void deleteComment(Long commentId, String email) {
        log.info("Starting deleting comment: id = {}", commentId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id " + commentId + " not found"));
        if (!comment.getUser().getEmail().equals(email)
                && user.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
        log.info("Comment deleted successfully");
    }
}

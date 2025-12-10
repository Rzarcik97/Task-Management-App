package taskmanagement.service;

import java.util.List;
import taskmanagement.dto.comment.CommentRequestDto;
import taskmanagement.dto.comment.CommentResponseDto;

public interface CommentService {

    CommentResponseDto addComment(CommentRequestDto request, String email);

    CommentResponseDto updateComment(Long id, String text, String email);

    void deleteComment(Long id, String email);

    List<CommentResponseDto> getCommentsByTask(Long taskId, String email);
}

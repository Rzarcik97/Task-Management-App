package taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import taskmanagement.dto.comment.CommentRequestDto;
import taskmanagement.dto.comment.CommentResponseDto;
import taskmanagement.service.CommentService;

@Log4j2
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add Comment",
            description = "Add a new comment to a task")
    public CommentResponseDto addComment(@Valid @RequestBody CommentRequestDto request,
                                         Authentication authentication) {
        String email = authentication.getName();
        log.info("Adding Comment to TasK {}, by User {}", request.taskId(), email);
        return commentService.addComment(request, email);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get Task Comments",
            description = "Retrieve all comments for a given task")
    public List<CommentResponseDto> getCommentsByTask(@PathVariable Long taskId,
                                                      Authentication authentication) {
        String email = authentication.getName();
        return commentService.getCommentsByTask(taskId, email);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update Comment",
            description = "Update your own comment by ID")
    public CommentResponseDto updateComment(@Valid @PathVariable Long commentId,
                                            @Valid @RequestParam("text") String text,
                                            Authentication authentication) {
        String email = authentication.getName();
        log.info("Editing Comment {}, by User {}", commentId, email);
        return commentService.updateComment(commentId, text, email);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete Comment",
            description = "Delete your own comment by ID")
    public void deleteComment(@Valid @PathVariable Long commentId,
                              Authentication authentication
    ) {
        String email = authentication.getName();
        log.info("Deleting Comment {}, by User {}", commentId, email);
        commentService.deleteComment(commentId, email);
    }
}

package taskmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import taskmanagement.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTask_Id(Long taskId);
}

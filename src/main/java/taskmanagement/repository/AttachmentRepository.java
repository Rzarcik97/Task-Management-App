package taskmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import taskmanagement.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTask_Id(Long taskId);

    List<Attachment> findByPath(String path);
}
